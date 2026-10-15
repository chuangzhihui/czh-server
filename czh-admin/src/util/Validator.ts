/**
 * 校验是否是正确的手机号
 * @param rule
 * @param value
 */
export const phoneValidate=(rule:any,value:any)=>{
  const reg = /^1[3456789]\d{9}$/;
  if(!reg.test(value))
  {
    return Promise.reject(new Error("请设置正确的手机号"))
  }
  return Promise.resolve();
}
/**
 * 校验是否为邮箱
 * @param rule
 * @param value
 */
export const checkEmail=(rule:any,value:any)=>{
  const regex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
  if(!regex.test(value)){
    return Promise.reject(new Error("请输入正确的邮箱"))
  }
  return Promise.resolve();
}
/**
 * 判断输入内容是否是一个正则表达式
 * @param rule
 * @param value
 */
export const checkRegExp=(rule:any,value:any)=>{
    const pattern:string = value;
  try {
      // 尝试使用字符串创建正则表达式对象
      new RegExp(pattern);
      return Promise.resolve();
  } catch (e) {
    // 创建失败则说明不是有效的正则表达式
    return Promise.reject(new Error("请输入正确的正则表达式"))
  }
}
/**
 * 校验输入内容是否为正整数
 * @param rule
 * @param value
 */
export const  isPositiveInteger=(rule:any,value:any)=>{
    const regex = /^[1-9]\d*$/;
    if(!regex.test(value)){
      return Promise.reject(new Error("请输入正整数"))
    }
    return Promise.resolve();
}
export const isCrontab=(rule:any,value:any)=>{
  const regex = /^(\*(\/[0-9]+)?|[0-9]+(-[0-9]+)?(,[0-9]+(-[0-9]+)?)*)\s+(\*(\/[0-9]+)?|[0-9]+(-[0-9]+)?(,[0-9]+(-[0-9]+)?)*)\s+(\*(\/[0-9]+)?|[0-9]+(-[0-9]+)?(,[0-9]+(-[0-9]+)?)*)\s+(\*(\/[0-9]+)?|[0-9]+(-[0-9]+)?(,[0-9]+(-[0-9]+)?)*)\s+(\*(\/[0-9]+)?|[0-9]+(-[0-9]+)?(,[0-9]+(-[0-9]+)?)*|(sun|mon|tue|wed|thu|fri|sat))\s+.+$/i;
  if(!regex.test(value)){
    return Promise.reject(new Error("请输入正确的定时任务规则"))
  }
  return Promise.resolve();
}
