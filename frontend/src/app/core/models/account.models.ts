export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'ONBOARDING';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type PotentialLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';
export type PriorityLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type AnalyticsLevel = RiskLevel | PotentialLevel | PriorityLevel;
export type OrderStatus = 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'DELAYED' | 'CANCELLED';
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'WAITING_CUSTOMER' | 'RESOLVED' | 'CLOSED';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type InteractionType = 'CALL' | 'EMAIL' | 'MEETING' | 'FOLLOW_UP' | 'NOTE';

export interface AccountWriteRequest {
  name: string;
  industry: string | null;
  region: string | null;
  status: AccountStatus;
  monthlyRevenue: number;
  previousMonthRevenue: number;
  engagementScore: number;
}

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

export interface OrderCreateRequest {
  orderNumber: string;
  amount: number;
  status: OrderStatus;
  orderedAt: string;
  expectedDeliveryAt: string | null;
  deliveredAt: string | null;
}

export interface OrderRecord extends OrderCreateRequest {
  id: string;
  accountId: string;
  createdAt: string;
}

export interface SupportTicketCreateRequest {
  subject: string;
  status: TicketStatus;
  priority: TicketPriority;
  openedAt: string;
  resolvedAt: string | null;
}

export interface SupportTicketRecord extends SupportTicketCreateRequest {
  id: string;
  accountId: string;
  createdAt: string;
}

export interface InteractionCreateRequest {
  type: InteractionType;
  summary: string;
  occurredAt: string;
}

export interface InteractionRecord extends InteractionCreateRequest {
  id: string;
  accountId: string;
  createdAt: string;
}
