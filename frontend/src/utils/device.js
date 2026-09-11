/**
 * 设备检测工具
 */
export function isMobile() {
  return window.innerWidth <= 767
}

export function isPC() {
  return window.innerWidth > 767
}
