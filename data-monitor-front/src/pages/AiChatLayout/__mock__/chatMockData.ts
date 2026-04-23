import type { ChatSession, ChatMessage } from '../../../types/ai-chat';

export const mockSessions: ChatSession[] = [
  {
    id: 'session-1',
    title: '请分析本季度营收趋势及异常波动原因',
    createdAt: '2025-04-20T10:30:00Z',
    updatedAt: '2025-04-20T10:35:00Z',
  },
  {
    id: 'session-2',
    title: '对比各行业收入占比变化',
    createdAt: '2025-04-19T14:20:00Z',
    updatedAt: '2025-04-19T14:28:00Z',
  },
  {
    id: 'session-3',
    title: '人员饱和度分析报告',
    createdAt: '2025-04-18T09:15:00Z',
    updatedAt: '2025-04-18T09:22:00Z',
  },
];

export const mockMessages: ChatMessage[] = [
  {
    id: 'msg-1',
    sessionId: 'session-1',
    role: 'user',
    chunks: [],
    content: '请分析本季度营收趋势及异常波动原因',
    timestamp: '2025-04-20T10:30:00Z',
  },
  {
    id: 'msg-2',
    sessionId: 'session-1',
    role: 'assistant',
    chunks: [
      {
        type: 'text',
        content:
          '## 本季度营收趋势分析\n\n根据数据分析，本季度营收呈现以下趋势：\n\n- **总营收**：累计达到 12,580 万元，同比增长 15.3%\n- **月度趋势**：1-2月稳步上升，3月出现明显波动\n- **净利率**：平均 18.7%，高于行业基准\n\n以下是营收趋势图表：',
      },
      {
        type: 'component',
        componentName: 'RevenueTrendChart',
        props: {
          months: ['1月', '2月', '3月'],
          revenue: [3850, 4120, 4610],
          netIncome: [720, 770, 860],
          title: '季度营收趋势',
        },
      },
      {
        type: 'text',
        content:
          '### 异常波动分析\n\n3月份营收出现 **12.1%** 的环比增长，主要原因如下：\n\n1. **新项目集中签约**：3月新增签约 8 个项目，贡献收入约 620 万元\n2. **行业政策利好**：政府数据要素政策推动市场需求增长\n3. **存量项目交付高峰**：多个跨年项目进入验收结算期\n\n> 建议关注 4 月份收入回落风险，提前做好现金流规划。',
      },
      {
        type: 'component',
        componentName: 'IndustryBreakdown',
        props: {
          industries: [
            { name: '政务', value: 35, color: '#00f3ff' },
            { name: '金融', value: 25, color: '#1890ff' },
            { name: '医疗', value: 18, color: '#36cfc9' },
            { name: '教育', value: 12, color: '#9254de' },
            { name: '其他', value: 10, color: '#8798af' },
          ],
          title: '行业收入占比',
        },
      },
      {
        type: 'text',
        content:
          '如需进一步深入分析某个行业或时间段的数据，请随时告诉我。',
      },
    ],
    content: '',
    timestamp: '2025-04-20T10:35:00Z',
  },
];
