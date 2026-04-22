import type { KpiSummary } from '../types/dashboard';

export const mockKpiSummary: KpiSummary = {
  annualContract: {
    title: '年度合同额',
    value: 12865.42,
    unit: '万元',
    comparisonLabel: '同比',
    comparisonValue: '+18.7%',
    trend: 'up',
  },
  annualRevenue: {
    title: '年度营业收入',
    value: 9742.68,
    unit: '万元',
    comparisonLabel: '目标',
    comparisonValue: '+24.4%',
    trend: 'up',
  },
  annualNetIncome: {
    title: '年度净收入',
    value: 3256.15,
    unit: '万元',
    comparisonLabel: '同比',
    comparisonValue: '+12.3%',
    trend: 'up',
  },
  contractCollectionRate: {
    title: '合同回款率',
    value: 87.6,
    unit: '%',
    comparisonLabel: '同比',
    comparisonValue: '+5.2%',
    trend: 'up',
  },
  perCapitaOutput: {
    title: '人均产值',
    value: 61.5,
    unit: '万元/人',
    comparisonLabel: '同比',
    comparisonValue: '+8.1%',
    trend: 'up',
  },
  assetLiabilityRatio: {
    title: '资产负债率',
    value: 42.3,
    unit: '%',
    comparisonLabel: '环比',
    comparisonValue: '-2.1%',
    trend: 'down',
  },
};
