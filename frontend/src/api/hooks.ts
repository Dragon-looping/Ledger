import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './client';
import type { CreateTransactionRequest, SetBudgetRequest } from './types';

export function useTransactions(params?: { month?: string; category?: string; type?: string }) {
  return useQuery({
    queryKey: ['transactions', params],
    queryFn: () => api.getTransactions(params),
  });
}

export function useMonthlySummary(month?: string) {
  return useQuery({
    queryKey: ['summary', month],
    queryFn: () => api.getSummary(month),
  });
}

export function useYearlyHeatmap(year?: number) {
  return useQuery({
    queryKey: ['heatmap', year],
    queryFn: () => api.getHeatmap(year),
  });
}

export function useBudgets(month?: string) {
  return useQuery({
    queryKey: ['budgets', month],
    queryFn: () => api.getBudgets(month),
  });
}

export function useCreateTransaction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateTransactionRequest) => api.createTransaction(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['summary'] });
      queryClient.invalidateQueries({ queryKey: ['heatmap'] });
    },
  });
}

export function useDeleteTransaction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteTransaction(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['summary'] });
      queryClient.invalidateQueries({ queryKey: ['heatmap'] });
    },
  });
}

export function useSetBudget() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: SetBudgetRequest) => api.setBudget(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['budgets'] });
      queryClient.invalidateQueries({ queryKey: ['summary'] });
    },
  });
}

export function useParseText() {
  return useMutation({
    mutationFn: (text: string) => api.parseText(text),
  });
}
