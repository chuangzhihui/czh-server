import { useEffect, useRef } from 'react';
import './index.scss';

/** 设备档位：粒子/节点数量与渲染精度按屏幕宽度分级，移动端显著降载 */
const TIER = {
  desktop: { dust: 700, nodes: 88, link: 2.6, dpr: 2 },
  tablet: { dust: 320, nodes: 56, link: 2.9, dpr: 1.75 },
  mobile: { dust: 140, nodes: 34, link: 3.2, dpr: 1.5 }
};

type TierKey = keyof typeof TIER;

const pickTier = (w: number): TierKey => (w >= 1200 ? 'desktop' : w >= 768 ? 'tablet' : 'mobile');

interface ThreeBackgroundProps {
  /** 主色（跟随 antd colorPrimary），用于星尘、连线与线框球 */
  color?: string;
}

/**
 * 登录页 WebGL 背景：星尘 + 节点连线网络 + 线框球体
 * - three 在 effect 内动态载入，组件本身由页面 lazy 引入，不进主包
 * - 页面隐藏 / 卸载时停止 RAF 并释放 GPU 资源
 * - 尊重 prefers-reduced-motion：仅渲染一帧静态画面
 */
const ThreeBackground = ({ color = '#4fc3f7' }: ThreeBackgroundProps) => {
  const hostRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const host = hostRef.current;
    if (!host) return;

    let disposed = false;
    let teardown: (() => void) | null = null;

    const boot = async () => {
      const THREE = await import('three');
      if (disposed || !host) return;

      const tier = TIER[pickTier(window.innerWidth)];
      const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      const coarse = window.matchMedia('(pointer: coarse)').matches;
      const main = new THREE.Color(color);
      const dim = main.clone().multiplyScalar(0.12);

      const renderer = new THREE.WebGLRenderer({ antialias: !coarse, alpha: true, powerPreference: 'high-performance' });
      renderer.setPixelRatio(Math.min(window.devicePixelRatio, tier.dpr));
      renderer.setSize(host.clientWidth, host.clientHeight);
      renderer.setClearColor(0x000000, 0);
      host.appendChild(renderer.domElement);

      const scene = new THREE.Scene();
      const camera = new THREE.PerspectiveCamera(60, host.clientWidth / host.clientHeight, 0.1, 100);
      camera.position.set(0, 0, 14);

      const world = new THREE.Group();
      scene.add(world);

      /* ---------- 1. 星尘 ---------- */
      const dotCanvas = document.createElement('canvas');
      dotCanvas.width = dotCanvas.height = 64;
      const ctx = dotCanvas.getContext('2d');
      if (ctx) {
        const grd = ctx.createRadialGradient(32, 32, 0, 32, 32, 32);
        grd.addColorStop(0, 'rgba(255,255,255,1)');
        grd.addColorStop(0.35, 'rgba(255,255,255,.5)');
        grd.addColorStop(1, 'rgba(255,255,255,0)');
        ctx.fillStyle = grd;
        ctx.fillRect(0, 0, 64, 64);
      }
      const dotMap = new THREE.CanvasTexture(dotCanvas);

      const dustPos = new Float32Array(tier.dust * 3);
      for (let i = 0; i < tier.dust; i++) {
        dustPos[i * 3] = (Math.random() - 0.5) * 34;
        dustPos[i * 3 + 1] = (Math.random() - 0.5) * 22;
        dustPos[i * 3 + 2] = (Math.random() - 0.5) * 18 - 4;
      }
      const dustGeo = new THREE.BufferGeometry();
      dustGeo.setAttribute('position', new THREE.BufferAttribute(dustPos, 3));
      const dustMat = new THREE.PointsMaterial({
        size: 0.09,
        map: dotMap,
        color: main,
        transparent: true,
        opacity: 0.9,
        depthWrite: false,
        blending: THREE.AdditiveBlending,
        sizeAttenuation: true
      });
      const dust = new THREE.Points(dustGeo, dustMat);
      world.add(dust);

      /* ---------- 2. 节点连线网络 ---------- */
      const n = tier.nodes;
      const nodePos = new Float32Array(n * 3);
      const nodeVel = new Float32Array(n * 3);
      const BOUND_X = 9;
      const BOUND_Y = 6;
      const BOUND_Z = 5;
      for (let i = 0; i < n; i++) {
        nodePos[i * 3] = (Math.random() - 0.5) * BOUND_X * 2;
        nodePos[i * 3 + 1] = (Math.random() - 0.5) * BOUND_Y * 2;
        nodePos[i * 3 + 2] = (Math.random() - 0.5) * BOUND_Z * 2;
        nodeVel[i * 3] = (Math.random() - 0.5) * 0.012;
        nodeVel[i * 3 + 1] = (Math.random() - 0.5) * 0.012;
        nodeVel[i * 3 + 2] = (Math.random() - 0.5) * 0.008;
      }

      const nodeGeo = new THREE.BufferGeometry();
      nodeGeo.setAttribute('position', new THREE.BufferAttribute(nodePos, 3));
      const nodeMat = new THREE.PointsMaterial({
        size: 0.16,
        map: dotMap,
        color: main,
        transparent: true,
        opacity: 1,
        depthWrite: false,
        blending: THREE.AdditiveBlending
      });
      const nodes = new THREE.Points(nodeGeo, nodeMat);
      world.add(nodes);

      // 连线预分配：每个节点最多 6 条，避免每帧重建 BufferGeometry
      const MAX_LINK = n * 6;
      const linkPos = new Float32Array(MAX_LINK * 6);
      const linkCol = new Float32Array(MAX_LINK * 6);
      const linkGeo = new THREE.BufferGeometry();
      const linkPosAttr = new THREE.BufferAttribute(linkPos, 3).setUsage(THREE.DynamicDrawUsage);
      const linkColAttr = new THREE.BufferAttribute(linkCol, 3).setUsage(THREE.DynamicDrawUsage);
      linkGeo.setAttribute('position', linkPosAttr);
      linkGeo.setAttribute('color', linkColAttr);
      const linkMat = new THREE.LineBasicMaterial({
        vertexColors: true,
        transparent: true,
        opacity: 0.55,
        depthWrite: false,
        blending: THREE.AdditiveBlending
      });
      const links = new THREE.LineSegments(linkGeo, linkMat);
      world.add(links);

      /* ---------- 3. 线框球体 ---------- */
      const sphereGeo = new THREE.IcosahedronGeometry(6.4, 1);
      const sphereMat = new THREE.MeshBasicMaterial({
        color: main,
        wireframe: true,
        transparent: true,
        opacity: 0.1,
        depthWrite: false
      });
      const sphere = new THREE.Mesh(sphereGeo, sphereMat);
      sphere.position.z = -6;
      world.add(sphere);

      /* ---------- 交互：鼠标视差 ---------- */
      const pointer = { x: 0, y: 0, tx: 0, ty: 0 };
      const onPointerMove = (e: PointerEvent) => {
        pointer.tx = (e.clientX / window.innerWidth - 0.5) * 2;
        pointer.ty = (e.clientY / window.innerHeight - 0.5) * 2;
      };
      if (!coarse && !reduceMotion) window.addEventListener('pointermove', onPointerMove, { passive: true });

      /* ---------- 尺寸 ---------- */
      let rafResize = 0;
      const resize = () => {
        cancelAnimationFrame(rafResize);
        rafResize = requestAnimationFrame(() => {
          const w = host.clientWidth;
          const h = host.clientHeight;
          if (!w || !h) return;
          camera.aspect = w / h;
          camera.updateProjectionMatrix();
          renderer.setSize(w, h);
          if (reduceMotion) renderer.render(scene, camera);
        });
      };
      window.addEventListener('resize', resize);

      /* ---------- 渲染循环 ---------- */
      let raf = 0;
      let running = false;
      const clock = new THREE.Clock();
      const tmpColor = new THREE.Color();

      const frame = () => {
        const dt = Math.min(clock.getDelta(), 0.05);
        const t = clock.elapsedTime;

        // 节点漂移 + 边界反弹
        for (let i = 0; i < n; i++) {
          const ix = i * 3;
          nodePos[ix] += nodeVel[ix] * dt * 60;
          nodePos[ix + 1] += nodeVel[ix + 1] * dt * 60;
          nodePos[ix + 2] += nodeVel[ix + 2] * dt * 60;
          if (Math.abs(nodePos[ix]) > BOUND_X) nodeVel[ix] *= -1;
          if (Math.abs(nodePos[ix + 1]) > BOUND_Y) nodeVel[ix + 1] *= -1;
          if (Math.abs(nodePos[ix + 2]) > BOUND_Z) nodeVel[ix + 2] *= -1;
        }
        nodeGeo.attributes.position.needsUpdate = true;

        // 邻近连线（含距离衰减配色）
        const maxDist = tier.link;
        let c = 0;
        for (let i = 0; i < n && c < MAX_LINK; i++) {
          for (let j = i + 1; j < n && c < MAX_LINK; j++) {
            const ax = nodePos[i * 3];
            const ay = nodePos[i * 3 + 1];
            const az = nodePos[i * 3 + 2];
            const bx = nodePos[j * 3];
            const by = nodePos[j * 3 + 1];
            const bz = nodePos[j * 3 + 2];
            const dx = ax - bx;
            const dy = ay - by;
            const dz = az - bz;
            const d2 = dx * dx + dy * dy + dz * dz;
            if (d2 > maxDist * maxDist) continue;
            const k = 1 - Math.sqrt(d2) / maxDist;
            const o = c * 6;
            linkPos[o] = ax;
            linkPos[o + 1] = ay;
            linkPos[o + 2] = az;
            linkPos[o + 3] = bx;
            linkPos[o + 4] = by;
            linkPos[o + 5] = bz;
            tmpColor.copy(dim).lerp(main, 0.15 + k * 0.85);
            linkCol[o] = linkCol[o + 3] = tmpColor.r;
            linkCol[o + 1] = linkCol[o + 4] = tmpColor.g;
            linkCol[o + 2] = linkCol[o + 5] = tmpColor.b;
            c++;
          }
        }
        linkGeo.setDrawRange(0, c * 2);
        linkPosAttr.needsUpdate = true;
        linkColAttr.needsUpdate = true;

        // 整体缓慢自转 + 呼吸，叠加鼠标视差
        pointer.x += (pointer.tx - pointer.x) * 0.045;
        pointer.y += (pointer.ty - pointer.y) * 0.045;
        world.rotation.y = t * 0.035 + pointer.x * 0.22;
        world.rotation.x = Math.sin(t * 0.12) * 0.06 + pointer.y * 0.14;
        sphere.rotation.x = t * 0.06;
        sphere.rotation.y = -t * 0.09;
        dust.rotation.y = -t * 0.02;
        dustMat.opacity = 0.7 + Math.sin(t * 0.8) * 0.18;

        renderer.render(scene, camera);
        raf = requestAnimationFrame(frame);
      };

      const start = () => {
        if (running || reduceMotion) return;
        running = true;
        clock.start();
        raf = requestAnimationFrame(frame);
      };
      const stop = () => {
        running = false;
        cancelAnimationFrame(raf);
      };
      const onVisibility = () => (document.hidden ? stop() : start());
      document.addEventListener('visibilitychange', onVisibility);

      if (reduceMotion) renderer.render(scene, camera);
      else start();

      teardown = () => {
        stop();
        document.removeEventListener('visibilitychange', onVisibility);
        window.removeEventListener('resize', resize);
        window.removeEventListener('pointermove', onPointerMove);
        cancelAnimationFrame(rafResize);
        scene.traverse(obj => {
          const node = obj as unknown as { geometry?: { dispose(): void }; material?: { dispose(): void } };
          node.geometry?.dispose();
          node.material?.dispose();
        });
        dotMap.dispose();
        renderer.dispose();
        renderer.domElement.remove();
      };
    };

    void boot();

    return () => {
      disposed = true;
      teardown?.();
    };
  }, [color]);

  return (
    <div className='threeBackground' ref={hostRef} aria-hidden='true'>
      <div className='threeBackgroundMask' />
    </div>
  );
};

export default ThreeBackground;
