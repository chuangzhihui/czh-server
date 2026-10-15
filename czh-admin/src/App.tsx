import { App as AntdApp, ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import { HashRouter as Router } from 'react-router-dom';
import { Suspense, useCallback, useMemo, useState } from 'react';
import CustomerRouter from './Route';
import Loading from './component/Loading';
import { ThemeContext, type ThemeContextValue } from './util/ThemeContext';
import { applyCssVars, getTheme, saveTheme, type ThemeToken } from './util/ThemeUtil';

dayjs.locale('zh-cn');

const Index = () => {
    const [token, setTokenState] = useState<ThemeToken>(() => getTheme());

    /** 只应用不落盘：主题弹窗实时预览 / 取消时还原 */
    const setToken = useCallback((next: ThemeToken) => {
        applyCssVars(next);
        setTokenState(next);
    }, []);

    /** 落盘并应用：主题弹窗点「确定」 */
    const saveToken = useCallback((next: ThemeToken) => {
        saveTheme(next);
        applyCssVars(next);
        setTokenState(next);
    }, []);

    const value = useMemo<ThemeContextValue>(() => ({ token, setToken, saveToken }), [token, setToken, saveToken]);

    return (
        <ConfigProvider locale={zhCN} theme={{ token }}>
            <ThemeContext.Provider value={value}>
                <AntdApp>
                    <Router>
                        <Suspense fallback={<Loading />}>
                            <CustomerRouter />
                        </Suspense>
                    </Router>
                </AntdApp>
            </ThemeContext.Provider>
        </ConfigProvider>
    );
};

export default Index;
