import type { FinanceTrends } from '../types/dashboard';

export const mockFinanceTrends: FinanceTrends = {
  months: [
    '2025.04', '2025.05', '2025.06', '2025.07',
    '2025.08', '2025.09', '2025.10', '2025.11',
    '2025.12', '2026.01', '2026.02', '2026.03',
  ],
  revenue: [780.5, 812.3, 856.7, 834.2, 798.6, 845.9, 872.1, 890.4, 923.7, 785.3, 756.8, 787.5],
  netIncome: [256.2, 278.4, 289.5, 271.8, 260.3, 285.6, 298.7, 310.2, 325.4, 248.9, 235.6, 255.8],
  costBudget: [520, 520, 520, 520, 520, 520, 520, 520, 520, 520, 520, 520],
  actualCost: [524.3, 533.9, 567.2, 562.4, 538.3, 560.3, 573.4, 580.2, 598.3, 536.4, 521.2, 531.7],
  personnel: {
    summary: {
      totalHeadcount: 158,
      internalStaff: 112,
      outsourcedStaff: 46,
      monthlyInvestment: 52.7,
    },
    saturation: [
      { month: '2025.04', rate: 78, headcount: 123 },
      { month: '2025.05', rate: 82, headcount: 130 },
      { month: '2025.06', rate: 85, headcount: 134 },
      { month: '2025.07', rate: 81, headcount: 128 },
      { month: '2025.08', rate: 76, headcount: 120 },
      { month: '2025.09', rate: 83, headcount: 131 },
      { month: '2025.10', rate: 87, headcount: 137 },
      { month: '2025.11', rate: 89, headcount: 141 },
      { month: '2025.12', rate: 92, headcount: 145 },
      { month: '2026.01', rate: 75, headcount: 118 },
      { month: '2026.02', rate: 72, headcount: 114 },
      { month: '2026.03', rate: 80, headcount: 126 },
    ],
  },
  resource: {
    summary: {
      serverCount: 386,
      computingCost: 127.5,
      aiServiceCost: 42.8,
    },
    charts: [
      { name: '计算资源', current: 72, target: 85 },
      { name: '存储资源', current: 68, target: 80 },
      { name: '网络资源', current: 55, target: 75 },
      { name: 'GPU资源', current: 89, target: 90 },
      { name: '内存资源', current: 76, target: 80 },
    ],
  },
};
