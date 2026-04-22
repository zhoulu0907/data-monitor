import { useEffect, useRef } from 'react';

/** 设计稿基准宽度 */
const DESIGN_WIDTH = 1920;
/** 设计稿基准高度 */
const DESIGN_HEIGHT = 1080;

/**
 * 大屏适配 Hook
 * 使用 CSS transform scale 实现等比例缩放
 */
export function useScreenAdapter() {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const updateScale = () => {
      if (!containerRef.current) return;
      const { innerWidth, innerHeight } = window;
      const scaleX = innerWidth / DESIGN_WIDTH;
      const scaleY = innerHeight / DESIGN_HEIGHT;
      const scale = Math.min(scaleX, scaleY);
      const container = containerRef.current;
      container.style.transform = `scale(${scale})`;
      container.style.transformOrigin = 'left top';

      // 居中偏移
      const offsetX = (innerWidth - DESIGN_WIDTH * scale) / 2;
      const offsetY = (innerHeight - DESIGN_HEIGHT * scale) / 2;
      container.style.marginLeft = `${offsetX}px`;
      container.style.marginTop = `${offsetY}px`;
    };

    updateScale();
    window.addEventListener('resize', updateScale);
    return () => window.removeEventListener('resize', updateScale);
  }, []);

  return containerRef;
}
