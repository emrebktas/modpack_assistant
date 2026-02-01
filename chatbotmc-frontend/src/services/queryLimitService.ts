// API base URL from environment variable with fallback for development
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

interface RemainingQueriesResponse {
  remainingQueries: number;
}

export const getRemainingQueries = async (): Promise<number> => {
  const token = localStorage.getItem('token');
  
  if (!token) {
    throw new Error('No authentication token found');
  }

  const response = await fetch(`${API_BASE_URL}/api/llm/remaining-queries`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('Failed to fetch remaining queries');
  }

  const data: RemainingQueriesResponse = await response.json();
  return data.remainingQueries;
};
