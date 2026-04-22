/** 统一 API 响应结构 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

/** 趋势方向 */
export type Trend = 'up' | 'down' | 'flat';

/** KPI 指标项 */
export interface KpiItem {
  title: string;
  value: number;
  unit: string;
  comparisonLabel: string;
  comparisonValue: string;
  trend: Trend;
}

/** 顶部 6 个核心 KPI */
export interface KpiSummary {
  annualContract: KpiItem;
  annualRevenue: KpiItem;
  annualNetIncome: KpiItem;
  contractCollectionRate: KpiItem;
  perCapitaOutput: KpiItem;
  assetLiabilityRatio: KpiItem;
}

/** 漏斗阶段 */
export interface FunnelStage {
  stage: string;
  value: number;
  rate: number;
}

/** 行业收入项 */
export interface IndustryItem {
  name: string;
  value: number;
  color: string;
}

/** 市场管线（漏斗 + 行业占比） */
export interface MarketPipeline {
  funnel: FunnelStage[];
  industryShare: IndustryItem[];
}

/** 财务趋势（12 个月） */
export interface FinanceTrends {
  months: string[];
  revenue: number[];
  netIncome: number[];
  costBudget: number[];
  actualCost: number[];
  personnel: PersonnelData;
  resource: ResourceUtilization;
}

/** 人员汇总 */
export interface PersonnelSummary {
  totalHeadcount: number;
  internalStaff: number;
  outsourcedStaff: number;
  monthlyInvestment: number;
}

/** 月度饱和度 */
export interface MonthlySaturation {
  month: string;
  rate: number;
  headcount: number;
}

/** 人员数据 */
export interface PersonnelData {
  summary: PersonnelSummary;
  saturation: MonthlySaturation[];
}

/** 资源汇总 */
export interface ResourceSummary {
  serverCount: number;
  computingCost: number;
  aiServiceCost: number;
}

/** 资源图表项 */
export interface ResourceChartItem {
  name: string;
  current: number;
  target: number;
}

/** 资源利用率 */
export interface ResourceUtilization {
  summary: ResourceSummary;
  charts: ResourceChartItem[];
}

/** 简报标签 */
export type BriefingTag = 'positive' | 'neutral' | 'warning';

/** AI 经营简报 */
export interface AiBriefing {
  id: string;
  tag: BriefingTag;
  title: string;
  summary: string;
  timestamp: string;
}

/** 风险等级 */
export type WarningLevel = 'high' | 'medium' | 'low';

/** AI 风险预警 */
export interface AiWarning {
  id: string;
  level: WarningLevel;
  title: string;
  scope: string;
  timestamp: string;
}

/** 聊天消息 */
export interface AiChatMessage {
  id: string;
  role: 'assistant' | 'user';
  content: string;
  timestamp: string;
}

/** AI 洞察（简报 + 预警 + 聊天） */
export interface AiInsights {
  briefings: AiBriefing[];
  warnings: AiWarning[];
  chatHistory: AiChatMessage[];
}

/** Header 导航项 */
export interface NavItem {
  key: string;
  label: string;
  icon: string;
}
