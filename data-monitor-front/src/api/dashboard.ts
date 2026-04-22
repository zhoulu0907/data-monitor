import axios from 'axios';
import type {
  ApiResponse,
  KpiSummary,
  MarketPipeline,
  FinanceTrends,
  AiInsights,
} from '../types/dashboard';

import { mockKpiSummary } from '../mocks/kpi';
import { mockMarketPipeline } from '../mocks/market';
import { mockFinanceTrends } from '../mocks/finance';
import { mockAiInsights } from '../mocks/ai';

/** 是否使用 Mock 数据（切换为 false 使用真实后端 API） */
const USE_MOCK = false;

const apiClient = axios.create({
  baseURL: '/api/v1/dashboard',
  timeout: 10000,
});

/** 获取顶部核心 KPI */
export async function fetchKpiSummary(): Promise<KpiSummary> {
  if (USE_MOCK) return mockKpiSummary;
  const res = await apiClient.get<ApiResponse<KpiSummary>>('/kpi/summary');
  return res.data.data;
}

/** 获取市场管线数据 */
export async function fetchMarketPipeline(): Promise<MarketPipeline> {
  if (USE_MOCK) return mockMarketPipeline;
  const res = await apiClient.get<ApiResponse<MarketPipeline>>('/market/pipeline');
  return res.data.data;
}

/** 获取财务趋势数据 */
export async function fetchFinanceTrends(): Promise<FinanceTrends> {
  if (USE_MOCK) return mockFinanceTrends;
  const res = await apiClient.get<ApiResponse<FinanceTrends>>('/finance/trends');
  return res.data.data;
}

/** 获取 AI 智能洞察 */
export async function fetchAiInsights(): Promise<AiInsights> {
  if (USE_MOCK) return mockAiInsights;
  const res = await apiClient.get<ApiResponse<AiInsights>>('/ai/insights');
  return res.data.data;
}
