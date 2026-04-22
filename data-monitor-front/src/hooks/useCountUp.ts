import { useEffect, useState } from 'react';

/**
 * 数字滚动动画 Hook
 * @param end 目标数值
 * @param duration 动画时长（毫秒）
 * @param decimals 小数位数
 */
export function useCountUp(end: number, duration = 1500, decimals = 2) {
  const [value, setValue] = useState(0);

  useEffect(() => {
    let startTime: number | null = null;
    let animationId: number;
    const startValue = 0;

    const animate = (timestamp: number) => {
      if (!startTime) startTime = timestamp;
      const progress = Math.min((timestamp - startTime) / duration, 1);
      // easeOutQuart 缓动
      const eased = 1 - Math.pow(1 - progress, 4);
      const current = startValue + (end - startValue) * eased;
      setValue(current);

      if (progress < 1) {
        animationId = requestAnimationFrame(animate);
      }
    };

    animationId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(animationId);
  }, [end, duration]);

  return decimals > 0 ? value.toFixed(decimals) : Math.round(value).toString();
}

/**
 * 数字格式化（千分位）
 */
export function formatNumber(num: number | string, decimals = 2): string {
  const n = typeof num === 'string' ? parseFloat(num) : num;
  if (isNaN(n)) return '0';
  return n.toLocaleString('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}
