import { create } from 'zustand';
import type {
  KpiSummary,
  MarketPipeline,
  FinanceTrends,
  AiInsights,
  AiChatMessage,
} from '../types/dashboard';
import {
  fetchKpiSummary,
  fetchMarketPipeline,
  fetchFinanceTrends,
  fetchAiInsights,
} from '../api/dashboard';

interface DashboardStore {
  // 数据状态
  kpiSummary: KpiSummary | null;
  marketPipeline: MarketPipeline | null;
  financeTrends: FinanceTrends | null;
  aiInsights: AiInsights | null;

  // UI 状态
  isAiDrawerOpen: boolean;
  isLoading: boolean;

  // 操作
  fetchAllData: () => Promise<void>;
  toggleAiDrawer: () => void;
  addUserMessage: (content: string) => void;
}

export const useDashboardStore = create<DashboardStore>((set, get) => ({
  kpiSummary: null,
  marketPipeline: null,
  financeTrends: null,
  aiInsights: null,
  isAiDrawerOpen: false,
  isLoading: false,

  fetchAllData: async () => {
    set({ isLoading: true });
    try {
      const [kpiSummary, marketPipeline, financeTrends, aiInsights] = await Promise.all([
        fetchKpiSummary(),
        fetchMarketPipeline(),
        fetchFinanceTrends(),
        fetchAiInsights(),
      ]);
      set({
        kpiSummary,
        marketPipeline,
        financeTrends,
        aiInsights,
        isLoading: false,
      });
    } catch (error) {
      console.error('加载数据失败:', error);
      set({ isLoading: false });
    }
  },

  toggleAiDrawer: () => {
    set((state) => ({ isAiDrawerOpen: !state.isAiDrawerOpen }));
  },

  addUserMessage: (content: string) => {
    const insights = get().aiInsights;
    if (!insights) return;

    const userMsg: AiChatMessage = {
      id: `user-${Date.now()}`,
      role: 'user',
      content,
      timestamp: new Date().toLocaleString('zh-CN'),
    };

    const aiMsg: AiChatMessage = {
      id: `ai-${Date.now()}`,
      role: 'assistant',
      content: '感谢您的提问，我正在分析相关数据。当前为演示模式，该功能将在后续版本中实现智能对话能力。',
      timestamp: new Date().toLocaleString('zh-CN'),
    };

    set({
      aiInsights: {
        ...insights,
        chatHistory: [...insights.chatHistory, userMsg, aiMsg],
      },
    });
  },
}));
