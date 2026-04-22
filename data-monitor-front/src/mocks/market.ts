import type { MarketPipeline } from '../types/dashboard';

export const mockMarketPipeline: MarketPipeline = {
  funnel: [
    { stage: '线索', value: 27010.89, rate: 100 },
    { stage: '商机', value: 18235.87, rate: 67.5 },
    { stage: '方案', value: 11892.34, rate: 65.2 },
    { stage: '招投标', value: 7536.21, rate: 63.4 },
    { stage: '签约', value: 4286.58, rate: 56.9 },
  ],
  industryShare: [
    { name: '政务', value: 38, color: '#1890ff' },
    { name: '交通物流', value: 16, color: '#00f3ff' },
    { name: '金融保险', value: 10, color: '#10b981' },
    { name: '制造业', value: 7, color: '#f59e0b' },
    { name: '能源电力', value: 6, color: '#8b5cf6' },
    { name: '教育', value: 5, color: '#ec4899' },
    { name: '医疗健康', value: 4, color: '#06b6d4' },
    { name: '其他', value: 14, color: '#6b7280' },
  ],
};
