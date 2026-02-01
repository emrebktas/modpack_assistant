// API base URL from environment variable with fallback for development
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const API_URL = `${API_BASE_URL}/api`;

export interface Conversation {
  id: number;
  title: string;
  createdAt: string;
  updatedAt: string;
  messageCount: number;
}

export interface ChatMessage {
  id: number;
  content: string;
  role: 'USER' | 'ASSISTANT';
  createdAt: string;
}

export const conversationService = {
  async getAllConversations(): Promise<Conversation[]> {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_URL}/conversations`, {
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch conversations');
    }
    
    return await response.json();
  },

  async getConversationMessages(conversationId: number): Promise<ChatMessage[]> {
    const token = localStorage.getItem('token');
    const response = await fetch(
      `${API_URL}/conversations/${conversationId}/messages`,
      { 
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        } 
      }
    );
    
    if (!response.ok) {
      throw new Error('Failed to fetch conversation messages');
    }
    
    return await response.json();
  },

  async createConversation(title: string): Promise<Conversation> {
    const token = localStorage.getItem('token');
    const response = await fetch(
      `${API_URL}/conversations`,
      {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ title })
      }
    );
    
    if (!response.ok) {
      throw new Error('Failed to create conversation');
    }
    
    return await response.json();
  },

  async deleteConversation(conversationId: number): Promise<void> {
    const token = localStorage.getItem('token');
    const response = await fetch(
      `${API_URL}/conversations/${conversationId}`,
      {
        method: 'DELETE',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    if (!response.ok) {
      throw new Error('Failed to delete conversation');
    }
  },

  async updateConversationTitle(conversationId: number, title: string): Promise<void> {
    const token = localStorage.getItem('token');
    const response = await fetch(
      `${API_URL}/conversations/${conversationId}/title`,
      {
        method: 'PATCH',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ title })
      }
    );
    
    if (!response.ok) {
      throw new Error('Failed to update conversation title');
    }
  }
};
