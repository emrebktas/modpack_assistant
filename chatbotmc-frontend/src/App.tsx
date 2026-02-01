import { useState, useRef, useEffect } from 'react'
import {
  Box,
  TextField,
  IconButton,
  Typography,
  ThemeProvider,
  createTheme,
  CssBaseline,
  Snackbar,
  Alert,
  Tooltip,
} from '@mui/material'
import SendIcon from '@mui/icons-material/Send'
import LoginDialog from './components/LoginDialog'
import RegisterDialog from './components/RegisterDialog'
import ConversationSidebar from './components/ConversationSidebar'
import { logout, isAuthenticated } from './services/authService'
import { conversationService, type Conversation } from './services/conversationService'
import { getRemainingQueries } from './services/queryLimitService'
import './App.css'

// API base URL from environment variable with fallback for development
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

interface Message {
  id: number
  text: string
  sender: 'user' | 'bot'
  timestamp: Date
}

// ChatGPT-style dark theme
const chatGptTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#10a37f',
      light: '#1a7f64',
      dark: '#0d8a6c',
    },
    secondary: {
      main: '#8e8ea0',
    },
    background: {
      default: '#212121',
      paper: '#2f2f2f',
    },
    text: {
      primary: '#ececec',
      secondary: '#8e8ea0',
    },
    divider: '#444654',
  },
  typography: {
    fontFamily: '"Söhne", "Inter", system-ui, -apple-system, sans-serif',
    h5: {
      fontWeight: 600,
    },
    body1: {
      fontSize: '1rem',
      lineHeight: 1.75,
    },
  },
  shape: {
    borderRadius: 12,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 500,
          borderRadius: 8,
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            '& fieldset': {
              borderColor: 'transparent',
            },
            '&:hover fieldset': {
              borderColor: 'transparent',
            },
            '&.Mui-focused fieldset': {
              borderColor: 'transparent',
            },
          },
        },
      },
    },
  },
})

function App() {
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [loginOpen, setLoginOpen] = useState(false)
  const [registerOpen, setRegisterOpen] = useState(false)
  const [authenticated, setAuthenticated] = useState(isAuthenticated())
  const [username, setUsername] = useState(localStorage.getItem('username') || '')
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarMessage, setSnackbarMessage] = useState('')
  const [snackbarSeverity, setSnackbarSeverity] = useState<'success' | 'error' | 'warning' | 'info'>('success')
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [currentConversationId, setCurrentConversationId] = useState<number | null>(null)
  const [remainingQueries, setRemainingQueries] = useState<number | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  useEffect(() => {
    if (authenticated) {
      loadConversations()
      loadRemainingQueries()
    }
  }, [authenticated])

  const loadRemainingQueries = async () => {
    try {
      const remaining = await getRemainingQueries()
      setRemainingQueries(remaining)
    } catch (error) {
      console.error('Error loading remaining queries:', error)
    }
  }

  const loadConversations = async () => {
    try {
      const convs = await conversationService.getAllConversations()
      setConversations(convs)
    } catch (error) {
      console.error('Error loading conversations:', error)
    }
  }

  const loadConversationMessages = async (conversationId: number) => {
    try {
      const msgs = await conversationService.getConversationMessages(conversationId)
      const formattedMessages: Message[] = msgs.map((msg) => ({
        id: msg.id,
        text: msg.content,
        sender: msg.role === 'USER' ? 'user' : 'bot',
        timestamp: new Date(msg.createdAt),
      }))
      setMessages(formattedMessages)
      setCurrentConversationId(conversationId)
    } catch (error) {
      console.error('Error loading messages:', error)
      setSnackbarMessage('Failed to load conversation')
      setSnackbarSeverity('error')
      setSnackbarOpen(true)
    }
  }

  const handleNewConversation = () => {
    setCurrentConversationId(null)
    setMessages([])
  }

  const handleDeleteConversation = async (conversationId: number) => {
    try {
      await conversationService.deleteConversation(conversationId)
      setConversations(conversations.filter((c) => c.id !== conversationId))
      if (currentConversationId === conversationId) {
        handleNewConversation()
      }
      setSnackbarMessage('Conversation deleted')
      setSnackbarSeverity('success')
      setSnackbarOpen(true)
    } catch (error) {
      console.error('Error deleting conversation:', error)
      setSnackbarMessage('Failed to delete conversation')
      setSnackbarSeverity('error')
      setSnackbarOpen(true)
    }
  }

  const handleRenameConversation = async (conversationId: number, newTitle: string) => {
    try {
      await conversationService.updateConversationTitle(conversationId, newTitle)
      setConversations(
        conversations.map((c) => (c.id === conversationId ? { ...c, title: newTitle } : c))
      )
      setSnackbarMessage('Conversation renamed')
      setSnackbarSeverity('success')
      setSnackbarOpen(true)
    } catch (error) {
      console.error('Error renaming conversation:', error)
      setSnackbarMessage('Failed to rename conversation')
      setSnackbarSeverity('error')
      setSnackbarOpen(true)
    }
  }

  const sendMessage = async () => {
    if (!input.trim() || loading) return

    if (!authenticated) {
      setSnackbarMessage('Please login to start chatting')
      setSnackbarSeverity('warning')
      setSnackbarOpen(true)
      setLoginOpen(true)
      return
    }

    const userMessage: Message = {
      id: Date.now(),
      text: input,
      sender: 'user',
      timestamp: new Date(),
    }

    setMessages((prev) => [...prev, userMessage])
    const currentInput = input
    setInput('')
    setLoading(true)

    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`${API_BASE_URL}/api/llm/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({ 
          prompt: currentInput,
          conversationId: currentConversationId 
        }),
      })

      if (response.status === 401 || response.status === 403) {
        logout()
        setAuthenticated(false)
        setUsername('')
        setSnackbarMessage('Session expired. Please login again.')
        setSnackbarSeverity('warning')
        setSnackbarOpen(true)
        setLoginOpen(true)
        return
      }

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to get response' }))
        throw new Error(errorData.message || 'Failed to get response')
      }

      const data = await response.json()
      
      loadRemainingQueries()

      const botMessage: Message = {
        id: data.messageId || Date.now() + 1,
        text: data.response,
        sender: 'bot',
        timestamp: new Date(),
      }

      setMessages((prev) => [...prev, botMessage])
      
      if (!currentConversationId && data.conversationId) {
        setCurrentConversationId(data.conversationId)
        loadConversations()
      }
    } catch (error) {
      console.error('Error:', error)
      const errorText = error instanceof Error ? error.message : 'Sorry, I encountered an error. Please try again.'
      const errorMessage: Message = {
        id: Date.now() + 1,
        text: errorText,
        sender: 'bot',
        timestamp: new Date(),
      }
      setMessages((prev) => [...prev, errorMessage])
      
      if (errorText.includes('query limit')) {
        setSnackbarMessage(errorText)
        setSnackbarSeverity('error')
        setSnackbarOpen(true)
      }
    } finally {
      setLoading(false)
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  const handleLoginSuccess = (user: string) => {
    setAuthenticated(true)
    setUsername(user)
    setSnackbarMessage(`Welcome back, ${user}!`)
    setSnackbarSeverity('success')
    setSnackbarOpen(true)
  }

  const handleRegisterSuccess = (user: string) => {
    setSnackbarMessage(`Registration successful, ${user}! Your account is pending admin approval.`)
    setSnackbarSeverity('info')
    setSnackbarOpen(true)
  }

  const handleLogout = () => {
    logout()
    setAuthenticated(false)
    setUsername('')
    setConversations([])
    setCurrentConversationId(null)
    setRemainingQueries(null)
    setMessages([])
    setSnackbarMessage('Logged out successfully')
    setSnackbarSeverity('success')
    setSnackbarOpen(true)
  }

  const handleSwitchToRegister = () => {
    setLoginOpen(false)
    setRegisterOpen(true)
  }

  const handleSwitchToLogin = () => {
    setRegisterOpen(false)
    setLoginOpen(true)
  }

  return (
    <ThemeProvider theme={chatGptTheme}>
      <CssBaseline />
      <Box
        sx={{
          height: '100vh',
          display: 'flex',
          bgcolor: '#212121',
        }}
      >
        {/* Sidebar */}
        <ConversationSidebar
          conversations={conversations}
          currentConversationId={currentConversationId}
          onSelectConversation={(id) => {
            if (id !== null) {
              loadConversationMessages(id)
            }
          }}
          onDeleteConversation={handleDeleteConversation}
          onRenameConversation={handleRenameConversation}
          onNewConversation={handleNewConversation}
          authenticated={authenticated}
          username={username}
          remainingQueries={remainingQueries}
          onLogin={() => setLoginOpen(true)}
          onLogout={handleLogout}
        />

        {/* Main Chat Area */}
        <Box
          sx={{
            flexGrow: 1,
            height: '100vh',
            display: 'flex',
            flexDirection: 'column',
            bgcolor: '#212121',
            position: 'relative',
          }}
        >
          {/* Messages Area */}
          <Box
            sx={{
              flexGrow: 1,
              overflow: 'auto',
              py: 4,
            }}
          >
            <Box sx={{ maxWidth: '768px', mx: 'auto', px: 4 }}>
              {messages.length === 0 ? (
                <Box
                  sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    height: '60vh',
                    gap: 3,
                  }}
                >
                  <Box
                    component="img"
                    src="/bettermc.jpg"
                    alt="BetterMC Logo"
                    sx={{
                      width: 301,
                      height: 161,
                      objectFit: 'contain',
                      borderRadius: '16px',
                    }}
                  />
                  <Typography
                    variant="h4"
                    sx={{
                      fontWeight: 600,
                      color: '#ececec',
                      textAlign: 'center',
                    }}
                  >
                    How can I help you today?
                  </Typography>
                  <Typography
                    variant="body1"
                    sx={{
                      color: '#8e8ea0',
                      textAlign: 'center',
                      maxWidth: 480,
                    }}
                  >
                    {authenticated 
                      ? "Ask me anything about BetterMC Modpack. I'm here to help!"
                      : "Please login to start chatting with the assistant."}
                  </Typography>
                </Box>
              ) : (
                messages.map((message) => (
                  <Box
                    key={message.id}
                    sx={{
                      py: 3,
                      '&:hover': {
                        bgcolor: 'rgba(255,255,255,0.02)',
                      },
                    }}
                  >
                    <Box sx={{ display: 'flex', gap: 3, alignItems: 'flex-start' }}>
                      {/* Avatar */}
                      <Box
                        sx={{
                          width: 32,
                          height: 32,
                          borderRadius: '4px',
                          bgcolor: message.sender === 'user' ? '#5436DA' : '#10a37f',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          flexShrink: 0,
                        }}
                      >
                        <Typography
                          sx={{
                            color: 'white',
                            fontSize: '14px',
                            fontWeight: 700,
                          }}
                        >
                          {message.sender === 'user' ? 'Y' : 'M'}
                        </Typography>
                      </Box>
                      
                      {/* Message Content */}
                      <Box sx={{ flex: 1, minWidth: 0 }}>
                        <Typography
                          variant="subtitle2"
                          sx={{
                            fontWeight: 600,
                            color: '#ececec',
                            mb: 0.5,
                          }}
                        >
                          {message.sender === 'user' ? 'You' : 'ModpackGPT'}
                        </Typography>
                        <Typography
                          className="message-content"
                          sx={{
                            color: '#d1d5db',
                            whiteSpace: 'pre-wrap',
                            wordBreak: 'break-word',
                            lineHeight: 1.75,
                          }}
                        >
                          {message.text}
                        </Typography>
                      </Box>
                    </Box>
                  </Box>
                ))
              )}
              
              {/* Loading indicator */}
              {loading && (
                <Box sx={{ py: 3 }}>
                  <Box sx={{ display: 'flex', gap: 3, alignItems: 'flex-start' }}>
                    <Box
                      sx={{
                        width: 32,
                        height: 32,
                        borderRadius: '4px',
                        bgcolor: '#10a37f',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0,
                      }}
                    >
                      <Typography sx={{ color: 'white', fontSize: '14px', fontWeight: 700 }}>
                        M
                      </Typography>
                    </Box>
                    <Box sx={{ flex: 1 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#ececec', mb: 0.5 }}>
                        ModpackGPT
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <Box
                          sx={{
                            width: 8,
                            height: 8,
                            borderRadius: '50%',
                            bgcolor: '#8e8ea0',
                            animation: 'pulse 1.4s ease-in-out infinite',
                            '@keyframes pulse': {
                              '0%, 80%, 100%': { opacity: 0.4 },
                              '40%': { opacity: 1 },
                            },
                          }}
                        />
                        <Box
                          sx={{
                            width: 8,
                            height: 8,
                            borderRadius: '50%',
                            bgcolor: '#8e8ea0',
                            animation: 'pulse 1.4s ease-in-out infinite',
                            animationDelay: '0.2s',
                            '@keyframes pulse': {
                              '0%, 80%, 100%': { opacity: 0.4 },
                              '40%': { opacity: 1 },
                            },
                          }}
                        />
                        <Box
                          sx={{
                            width: 8,
                            height: 8,
                            borderRadius: '50%',
                            bgcolor: '#8e8ea0',
                            animation: 'pulse 1.4s ease-in-out infinite',
                            animationDelay: '0.4s',
                            '@keyframes pulse': {
                              '0%, 80%, 100%': { opacity: 0.4 },
                              '40%': { opacity: 1 },
                            },
                          }}
                        />
                      </Box>
                    </Box>
                  </Box>
                </Box>
              )}
              <div ref={messagesEndRef} />
            </Box>
          </Box>

          {/* Input Area */}
          <Box
            sx={{
              position: 'sticky',
              bottom: 0,
              bgcolor: '#212121',
              pt: 2,
              pb: 3,
              px: 4,
            }}
          >
            <Box sx={{ maxWidth: '768px', mx: 'auto' }}>
              <Box
                sx={{
                  display: 'flex',
                  alignItems: 'flex-end',
                  gap: 1,
                  bgcolor: '#2f2f2f',
                  borderRadius: '26px',
                  border: '1px solid #424242',
                  px: 2,
                  py: 1,
                  transition: 'border-color 0.2s',
                  '&:focus-within': {
                    borderColor: '#565656',
                  },
                }}
              >
                <TextField
                  fullWidth
                  multiline
                  maxRows={6}
                  placeholder={
                    !authenticated 
                      ? "Login to start chatting..." 
                      : remainingQueries === 0 
                      ? "Query limit reached" 
                      : "Message ModpackGPT..."
                  }
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyPress={handleKeyPress}
                  disabled={loading || !authenticated || remainingQueries === 0}
                  variant="standard"
                  InputProps={{
                    disableUnderline: true,
                  }}
                  sx={{
                    '& .MuiInputBase-input': {
                      color: '#ececec',
                      fontSize: '1rem',
                      lineHeight: 1.5,
                      py: 1,
                      '&::placeholder': {
                        color: '#8e8ea0',
                        opacity: 1,
                      },
                    },
                  }}
                />
                <Tooltip title={!authenticated ? "Login required" : remainingQueries === 0 ? "No queries remaining" : "Send message"}>
                  <span>
                    <IconButton
                      onClick={sendMessage}
                      disabled={!input.trim() || loading || !authenticated || remainingQueries === 0}
                      sx={{
                        bgcolor: input.trim() && !loading && authenticated && remainingQueries !== 0 
                          ? '#10a37f' 
                          : 'transparent',
                        color: input.trim() && !loading && authenticated && remainingQueries !== 0 
                          ? 'white' 
                          : '#565656',
                        width: 36,
                        height: 36,
                        mb: 0.5,
                        transition: 'all 0.2s',
                        '&:hover': {
                          bgcolor: input.trim() && !loading && authenticated && remainingQueries !== 0 
                            ? '#0d8a6c' 
                            : 'rgba(255,255,255,0.05)',
                        },
                        '&.Mui-disabled': {
                          color: '#565656',
                        },
                      }}
                    >
                      <SendIcon sx={{ fontSize: 20 }} />
                    </IconButton>
                  </span>
                </Tooltip>
              </Box>
              
              <Typography
                variant="caption"
                sx={{
                  display: 'block',
                  textAlign: 'center',
                  color: '#8e8ea0',
                  mt: 1.5,
                  fontSize: '12px',
                }}
              >
                {authenticated && remainingQueries !== null && (
                  <span style={{ color: remainingQueries <= 2 ? '#f59e0b' : '#8e8ea0' }}>
                    {remainingQueries} queries remaining • 
                  </span>
                )}
                {' '}ModpackGPT can make mistakes. Verify important information.
              </Typography>
            </Box>
          </Box>
        </Box>
      </Box>

      {/* Dialogs */}
      <LoginDialog
        open={loginOpen}
        onClose={() => setLoginOpen(false)}
        onLoginSuccess={handleLoginSuccess}
        onSwitchToRegister={handleSwitchToRegister}
      />

      <RegisterDialog
        open={registerOpen}
        onClose={() => setRegisterOpen(false)}
        onRegisterSuccess={handleRegisterSuccess}
        onSwitchToLogin={handleSwitchToLogin}
      />

      {/* Snackbar */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={snackbarSeverity === 'error' ? 6000 : 4000}
        onClose={() => setSnackbarOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert 
          onClose={() => setSnackbarOpen(false)} 
          severity={snackbarSeverity} 
          sx={{ 
            width: '100%',
            bgcolor: snackbarSeverity === 'success' ? '#10a37f' : undefined,
          }}
        >
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </ThemeProvider>
  )
}

export default App
