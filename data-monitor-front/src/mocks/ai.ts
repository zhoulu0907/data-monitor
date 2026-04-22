import type { AiInsights } from '../types/dashboard';

export const mockAiInsights: AiInsights = {
  briefings: [
    {
      id: 'b001',
      tag: 'positive',
      title: '营收增长超预期',
      summary: '本月营收环比增长12.3%，超额完成季度目标的108%，主要贡献来自政务和金融行业。',
      timestamp: '2026-04-22 09:30:00',
    },
    {
      id: 'b002',
      tag: 'warning',
      title: '交通物流行业收入下滑',
      summary: '交通物流行业收入占比下降3.2%，较上季度减少约180万元，建议关注大客户续约情况。',
      timestamp: '2026-04-22 09:30:00',
    },
    {
      id: 'b003',
      tag: 'positive',
      title: '回款率持续提升',
      summary: '合同回款率达87.6%，同比提升5.2个百分点，回款周期缩短至平均42天。',
      timestamp: '2026-04-22 09:30:00',
    },
  ],
  warnings: [
    {
      id: 'w001',
      level: 'high',
      title: 'XX项目成本超支',
      scope: '影响季度利润约120万元',
      timestamp: '2026-04-22 08:00:00',
    },
    {
      id: 'w002',
      level: 'medium',
      title: '外协人员占比接近预警线',
      scope: '当前29.1%，预警线30%，涉及交通物流和制造业项目',
      timestamp: '2026-04-22 08:00:00',
    },
    {
      id: 'w003',
      level: 'low',
      title: 'GPU资源利用率偏高',
      scope: '当前89%，建议在Q3前完成扩容规划',
      timestamp: '2026-04-22 08:00:00',
    },
  ],
  chatHistory: [
    {
      id: 'c001',
      role: 'assistant',
      content:
        '您好，我是AI智策中枢。基于当前经营数据分析，我为您整理了以下要点：\n\n1. 本月营收表现优异，环比增长12.3%\n2. 需关注交通物流行业收入下滑趋势\n3. XX项目成本超支需及时干预\n\n请问您想深入了解哪方面的数据？',
      timestamp: '2026-04-22 09:00:00',
    },
  ],
};
