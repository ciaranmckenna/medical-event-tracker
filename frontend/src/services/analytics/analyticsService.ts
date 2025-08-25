import { apiClient } from '../api/apiClient';
import type {
  MedicationCorrelationAnalysis,
  MedicationImpactAnalysis,
  TimelineAnalysis,
  DashboardSummary
} from '../../types/medical';

/**
 * Analytics service matching the new backend architecture
 * Provides correlation, dashboard, and timeline analysis
 */
export class AnalyticsService {
  
  // Correlation Analysis Endpoints
  async getMedicationCorrelation(
    patientId: string, 
    medicationId: string
  ): Promise<MedicationCorrelationAnalysis> {
    try {
      return await apiClient.get<MedicationCorrelationAnalysis>(
        `/api/analytics/correlation/${patientId}/${medicationId}`
      );
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to fetch medication correlation analysis';
      throw new Error(message);
    }
  }

  async getAllMedicationCorrelations(patientId: string): Promise<MedicationCorrelationAnalysis[]> {
    try {
      return await apiClient.get<MedicationCorrelationAnalysis[]>(
        `/api/analytics/correlation/${patientId}`
      );
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to fetch medication correlations';
      throw new Error(message);
    }
  }

  async getMedicationImpactAnalysis(
    patientId: string,
    medicationId: string,
    startDate: string,
    endDate: string
  ): Promise<MedicationImpactAnalysis> {
    try {
      return await apiClient.get<MedicationImpactAnalysis>(
        `/api/analytics/impact/${patientId}/${medicationId}`,
        {
          params: { startDate, endDate }
        }
      );
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to fetch medication impact analysis';
      throw new Error(message);
    }
  }

  // Dashboard Analytics Endpoints
  async getDashboardSummary(patientId: string): Promise<DashboardSummary> {
    try {
      return await apiClient.get<DashboardSummary>(
        `/api/analytics/dashboard/${patientId}`
      );
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to fetch dashboard summary';
      throw new Error(message);
    }
  }

  async getWeeklySummaries(patientId: string): Promise<Record<string, DashboardSummary>> {
    try {
      return await apiClient.get<Record<string, DashboardSummary>>(
        `/api/analytics/dashboard/${patientId}/weekly`
      );
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to fetch weekly summaries';
      throw new Error(message);
    }
  }

  // Timeline Analysis Endpoints
  async getTimelineAnalysis(
    patientId: string,
    startDate: string,
    endDate: string
  ): Promise<TimelineAnalysis> {
    try {
      return await apiClient.get<TimelineAnalysis>(
        `/api/analytics/timeline/${patientId}`,
        {
          params: { startDate, endDate }
        }
      );
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to fetch timeline analysis';
      throw new Error(message);
    }
  }

  async getMedicationTimeline(
    patientId: string,
    medicationId: string,
    startDate: string,
    endDate: string
  ): Promise<TimelineAnalysis> {
    try {
      return await apiClient.get<TimelineAnalysis>(
        `/api/analytics/timeline/${patientId}/medication/${medicationId}`,
        {
          params: { startDate, endDate }
        }
      );
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to fetch medication timeline';
      throw new Error(message);
    }
  }

  // Utility Methods for Chart Data Processing
  processCorrelationData(correlations: MedicationCorrelationAnalysis[]) {
    return correlations.map(correlation => ({
      medication: correlation.medicationName,
      correlationPercentage: correlation.correlationPercentage,
      strength: correlation.correlationStrength,
      totalDosages: correlation.totalDosages,
      eventsTriggered: correlation.eventsWithin24Hours,
      riskLevel: this.calculateRiskLevel(correlation.correlationStrength),
      categoryBreakdown: correlation.eventsByCategory,
      severityBreakdown: correlation.eventsBySeverity
    }));
  }

  processTimelineDataForChart(timeline: TimelineAnalysis) {
    return timeline.dataPoints.map(point => ({
      timestamp: new Date(point.timestamp),
      type: point.eventType.toLowerCase(),
      description: point.description,
      value: point.value || 0,
      unit: point.unit || '',
      severity: point.severity || 'MILD',
      category: point.eventType === 'EVENT' ? 'medical_event' : 'medication_dosage'
    }));
  }

  processDashboardMetrics(dashboard: DashboardSummary) {
    const totalEvents = dashboard.totalEvents;
    const totalDosages = dashboard.totalDosages;
    const recentActivity = dashboard.recentEvents + dashboard.recentDosages;

    return {
      overview: {
        totalEvents,
        totalDosages,
        recentActivity,
        eventRate: totalDosages > 0 ? (totalEvents / totalDosages) : 0
      },
      categoryDistribution: Object.entries(dashboard.eventsByCategory).map(([category, count]) => ({
        category,
        count,
        percentage: totalEvents > 0 ? (count / totalEvents) * 100 : 0
      })),
      severityDistribution: Object.entries(dashboard.eventsBySeverity).map(([severity, count]) => ({
        severity,
        count,
        percentage: totalEvents > 0 ? (count / totalEvents) * 100 : 0
      })),
      weeklyTrends: Object.entries(dashboard.weeklyStatistics).map(([week, count]) => ({
        week,
        count,
        date: new Date(week)
      }))
    };
  }

  // Analytics Helper Methods
  private calculateRiskLevel(correlationStrength: number): 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL' {
    if (correlationStrength >= 0.8) return 'CRITICAL';
    if (correlationStrength >= 0.6) return 'HIGH';
    if (correlationStrength >= 0.4) return 'MODERATE';
    return 'LOW';
  }

  calculateTrends(data: any[], field: string, periods: number = 7) {
    if (data.length < periods) return { trend: 'INSUFFICIENT_DATA', change: 0 };

    const recent = data.slice(-periods);
    const previous = data.slice(-periods * 2, -periods);

    if (previous.length === 0) return { trend: 'INSUFFICIENT_DATA', change: 0 };

    const recentAvg = recent.reduce((sum, item) => sum + (item[field] || 0), 0) / recent.length;
    const previousAvg = previous.reduce((sum, item) => sum + (item[field] || 0), 0) / previous.length;

    const change = previousAvg > 0 ? ((recentAvg - previousAvg) / previousAvg) * 100 : 0;

    let trend: 'IMPROVING' | 'STABLE' | 'WORSENING' = 'STABLE';
    if (Math.abs(change) > 10) {
      trend = change > 0 ? 'WORSENING' : 'IMPROVING';
    }

    return { trend, change: Math.round(change * 100) / 100 };
  }

  // Date utility methods for analytics
  getDateRange(period: 'week' | 'month' | 'quarter' | 'year'): { start: string; end: string } {
    const end = new Date();
    const start = new Date();

    switch (period) {
      case 'week':
        start.setDate(end.getDate() - 7);
        break;
      case 'month':
        start.setMonth(end.getMonth() - 1);
        break;
      case 'quarter':
        start.setMonth(end.getMonth() - 3);
        break;
      case 'year':
        start.setFullYear(end.getFullYear() - 1);
        break;
    }

    return {
      start: start.toISOString().split('T')[0],
      end: end.toISOString().split('T')[0]
    };
  }

  formatAnalyticsDate(date: Date | string): string {
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.toISOString().split('T')[0];
  }

  // Validation methods
  validateAnalyticsRequest(patientId: string, startDate?: string, endDate?: string) {
    if (!patientId || patientId.trim() === '') {
      throw new Error('Patient ID is required for analytics');
    }

    if (startDate && endDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);

      if (start >= end) {
        throw new Error('Start date must be before end date');
      }

      if (end > new Date()) {
        throw new Error('End date cannot be in the future');
      }

      // Limit analysis to 2 years for performance
      const maxRange = new Date();
      maxRange.setFullYear(maxRange.getFullYear() - 2);
      if (start < maxRange) {
        throw new Error('Analysis period cannot exceed 2 years');
      }
    }
  }
}

// Create and export singleton instance
export const analyticsService = new AnalyticsService();
export default analyticsService;