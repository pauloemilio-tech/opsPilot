export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'ONBOARDING';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type PotentialLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';
export type PriorityLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type AnalyticsLevel = RiskLevel | PotentialLevel | PriorityLevel;

export interface AccountSummary {
  id: string;
  name: string;
  industry: string | null;
  region: string | null;
  status: AccountStatus;
  monthlyRevenue: number;
  previousMonthRevenue: number;
  engagementScore: number;
}

export interface AccountDetails extends AccountSummary {
  revenueChangePercentage: number;
  createdAt: string;
  updatedAt: string;
}

export interface AccountOperationalSnapshot {
  accountId: string;
  accountName: string;
  monthlyRevenue: number;
  previousMonthRevenue: number;
  revenueChangePercentage: number;
  engagementScore: number;
  delayedOrders: number;
  openTickets: number;
  criticalOpenTickets: number;
  lastInteractionAt: string | null;
}

export interface RiskAssessment {
  score: number;
  level: RiskLevel;
  revenueRisk: number;
  delayedOrdersRisk: number;
  supportTicketsRisk: number;
  criticalTicketsRisk: number;
  engagementRisk: number;
  inactivityRisk: number;
}

export interface PotentialAssessment {
  score: number;
  level: PotentialLevel;
  revenueGrowthPotential: number;
  engagementPotential: number;
  revenueStrengthPotential: number;
  interactionPotential: number;
  operationalStabilityPotential: number;
}

export interface AccountAnalytics {
  accountId: string;
  accountName: string;
  risk: RiskAssessment;
  potential: PotentialAssessment;
  priorityScore: number;
  priorityLevel: PriorityLevel;
}

export interface AccountPriority {
  accountId: string;
  accountName: string;
  riskScore: number;
  riskLevel: RiskLevel;
  potentialScore: number;
  potentialLevel: PotentialLevel;
  priorityScore: number;
  priorityLevel: PriorityLevel;
}
