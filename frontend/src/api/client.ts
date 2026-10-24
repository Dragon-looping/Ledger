import type {
  Budget,
  CreateTransactionRequest,
  HeatmapItem,
  MonthlySummary,
  ParseResponse,
  SetBudgetRequest,
  Transaction,
} from './types';

const BASE_URL = '/api';

async function request<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!res.ok) {
    let errorMsg = `HTTP Error ${res.status}`;
    try {
      const errJson = await res.json();
      errorMsg = errJson.message || errJson.error || errorMsg;
    } catch {
      // ignore non-json error responses
    }
    throw new Error(errorMsg);
  }

  // Handle 204 No Content
  if (res.status === 204) {
    return {} as T;
  }

  return res.json();
}

export const api = {
  getTransactions: (params?: { month?: string; category?: string; type?: string }) => {
    const query = new URLSearchParams();
    if (params?.month) query.set('month', params.month);
    if (params?.category) query.set('category', params.category);
    if (params?.type) query.set('type', params.type);
    const qs = query.toString();
    return request<Transaction[]>(`/transactions${qs ? `?${qs}` : ''}`);
  },

  getTransactionById: (id: number) => {
    return request<Transaction>(`/transactions/${id}`);
  },

  createTransaction: (data: CreateTransactionRequest) => {
    return request<Transaction>('/transactions', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  deleteTransaction: (id: number) => {
    return request<void>(`/transactions/${id}`, {
      method: 'DELETE',
    });
  },

  getSummary: (month?: string) => {
    const qs = month ? `?month=${encodeURIComponent(month)}` : '';
    return request<MonthlySummary>(`/summary${qs}`);
  },

  getHeatmap: (year?: number) => {
    const qs = year ? `?year=${year}` : '';
    return request<HeatmapItem[]>(`/heatmap${qs}`);
  },

  getBudgets: (month?: string) => {
    const qs = month ? `?month=${encodeURIComponent(month)}` : '';
    return request<Budget[]>(`/budgets${qs}`);
  },

  setBudget: (data: SetBudgetRequest) => {
    return request<Budget>('/budgets', {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  parseText: (text: string) => {
    return request<ParseResponse>('/parse', {
      method: 'POST',
      body: JSON.stringify({ text }),
    });
  },
};
