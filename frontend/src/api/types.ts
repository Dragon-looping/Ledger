export type TransactionType = 'EXPENSE' | 'INCOME' | 'RECURRING_EXPENSE';

export type Category =
  | 'FOOD'
  | 'TRANSPORT'
  | 'RENT'
  | 'SHOPPING'
  | 'BILLS'
  | 'HEALTH'
  | 'ENTERTAINMENT'
  | 'SALARY'
  | 'OTHER';

export type Frequency = 'WEEKLY' | 'MONTHLY' | 'YEARLY';

export interface Transaction {
  id: number;
  type: TransactionType;
  amount: number;
  currency: string;
  signedAmount: number;
  date: string;
  note: string;
  category: Category;
  categoryIcon: string;
  categoryHexColor: string;
  frequency: Frequency | null;
  description: string;
}

export interface CreateTransactionRequest {
  type: TransactionType;
  amount: number;
  currency?: string;
  date: string;
  note?: string;
  category: Category;
  frequency?: Frequency | null;
}

export interface CategoryBreakdown {
  category: Category;
  categoryName: string;
  icon: string;
  hexColor: string;
  spent: number;
  percentage: number;
}

export interface BudgetStatus {
  ruleName: string;
  category: Category | null;
  limit: number;
  spent: number;
  percentUsed: number;
  level: 'OK' | 'WARNING' | 'EXCEEDED';
}

export interface MonthlySummary {
  period: string;
  currency: string;
  totalIncome: number;
  totalSpent: number;
  netBalance: number;
  savingsRate: number;
  transactionCount: number;
  categoryBreakdown: CategoryBreakdown[];
  budgetStatuses: BudgetStatus[];
}

export interface HeatmapItem {
  date: string;
  amount: number;
  count: number;
  level: number; // 0 to 4
}

export interface Budget {
  id: number;
  category: Category;
  amount: number;
  currency: string;
  month: string;
}

export interface SetBudgetRequest {
  category: Category;
  amount: number;
  currency?: string;
  month?: string;
}

export interface ParseResponse {
  amount: number;
  currency: string;
  date: string;
  note: string;
  category: Category;
  type: TransactionType;
  categoryIcon: string;
  categoryHexColor: string;
  rawInput: string;
}

export interface BudgetAlertEvent {
  level: 'WARNING' | 'EXCEEDED';
  ruleName: string;
  period: string;
  limit: number;
  spent: number;
  percentUsed: number;
  currency: string;
  triggeringTransaction: string;
  timestamp: string;
}
