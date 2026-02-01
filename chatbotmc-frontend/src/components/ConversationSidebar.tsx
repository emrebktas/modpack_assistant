import { useState } from 'react'
import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  IconButton,
  Typography,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Tooltip,
} from '@mui/material'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import AddIcon from '@mui/icons-material/Add'
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline'
import LogoutIcon from '@mui/icons-material/Logout'
import LoginIcon from '@mui/icons-material/Login'
import PersonOutlineIcon from '@mui/icons-material/PersonOutline'
import { type Conversation } from '../services/conversationService'

interface ConversationSidebarProps {
  conversations: Conversation[]
  currentConversationId: number | null
  onSelectConversation: (id: number | null) => void
  onDeleteConversation: (id: number) => void
  onRenameConversation: (id: number, newTitle: string) => void
  onNewConversation: () => void
  authenticated: boolean
  username: string
  remainingQueries: number | null
  onLogin: () => void
  onLogout: () => void
}

export default function ConversationSidebar({
  conversations,
  currentConversationId,
  onSelectConversation,
  onDeleteConversation,
  onRenameConversation,
  onNewConversation,
  authenticated,
  username,
  remainingQueries,
  onLogin,
  onLogout,
}: ConversationSidebarProps) {
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editTitle, setEditTitle] = useState('')
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [conversationToDelete, setConversationToDelete] = useState<number | null>(null)
  const [hoveredId, setHoveredId] = useState<number | null>(null)

  const handleEditClick = (conv: Conversation, e: React.MouseEvent) => {
    e.stopPropagation()
    setEditingId(conv.id)
    setEditTitle(conv.title)
  }

  const handleSaveEdit = () => {
    if (editingId && editTitle.trim()) {
      onRenameConversation(editingId, editTitle.trim())
      setEditingId(null)
    }
  }

  const handleCancelEdit = () => {
    setEditingId(null)
    setEditTitle('')
  }

  const handleDeleteClick = (id: number, e: React.MouseEvent) => {
    e.stopPropagation()
    setConversationToDelete(id)
    setDeleteDialogOpen(true)
  }

  const handleConfirmDelete = () => {
    if (conversationToDelete) {
      onDeleteConversation(conversationToDelete)
      setDeleteDialogOpen(false)
      setConversationToDelete(null)
    }
  }

  return (
    <>
      <Box
        sx={{
          width: 260,
          height: '100vh',
          bgcolor: '#171717',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
      >
        {/* New Chat Button */}
        <Box sx={{ p: 2 }}>
          <Button
            fullWidth
            variant="outlined"
            startIcon={<AddIcon />}
            onClick={onNewConversation}
            disabled={!authenticated}
            sx={{
              color: '#ececec',
              borderColor: '#424242',
              justifyContent: 'flex-start',
              py: 1.25,
              px: 2,
              fontWeight: 500,
              '&:hover': {
                bgcolor: '#2f2f2f',
                borderColor: '#565656',
              },
              '&.Mui-disabled': {
                color: '#565656',
                borderColor: '#2f2f2f',
              },
            }}
          >
            New chat
          </Button>
        </Box>

        {/* Conversations List */}
        <Box sx={{ flexGrow: 1, overflow: 'auto', px: 1 }}>
          {!authenticated ? (
            <Box sx={{ p: 3, textAlign: 'center' }}>
              <ChatBubbleOutlineIcon sx={{ fontSize: 40, color: '#565656', mb: 2 }} />
              <Typography variant="body2" sx={{ color: '#8e8ea0' }}>
                Login to view your conversations
              </Typography>
            </Box>
          ) : conversations.length === 0 ? (
            <Box sx={{ p: 3, textAlign: 'center' }}>
              <ChatBubbleOutlineIcon sx={{ fontSize: 40, color: '#565656', mb: 2 }} />
              <Typography variant="body2" sx={{ color: '#8e8ea0' }}>
                No conversations yet
              </Typography>
            </Box>
          ) : (
            <>
              <Typography
                variant="caption"
                sx={{
                  px: 2,
                  py: 1,
                  display: 'block',
                  color: '#8e8ea0',
                  fontWeight: 500,
                }}
              >
                Recent
              </Typography>
              <List sx={{ p: 0 }}>
                {conversations.map((conv) => (
                  <ListItem
                    key={conv.id}
                    disablePadding
                    onMouseEnter={() => setHoveredId(conv.id)}
                    onMouseLeave={() => setHoveredId(null)}
                    sx={{ mb: 0.5 }}
                  >
                    <ListItemButton
                      selected={currentConversationId === conv.id}
                      onClick={() => onSelectConversation(conv.id)}
                      sx={{
                        borderRadius: 2,
                        py: 1.25,
                        px: 2,
                        '&:hover': {
                          bgcolor: '#2f2f2f',
                        },
                        '&.Mui-selected': {
                          bgcolor: '#2f2f2f',
                          '&:hover': {
                            bgcolor: '#353535',
                          },
                        },
                      }}
                    >
                      <ListItemText
                        primary={
                          <Typography
                            variant="body2"
                            sx={{
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                              color: '#ececec',
                              fontWeight: currentConversationId === conv.id ? 500 : 400,
                              pr: hoveredId === conv.id ? 7 : 0,
                            }}
                          >
                            {conv.title}
                          </Typography>
                        }
                      />
                      {hoveredId === conv.id && (
                        <Box
                          sx={{
                            position: 'absolute',
                            right: 8,
                            display: 'flex',
                            gap: 0.25,
                            bgcolor: '#2f2f2f',
                          }}
                        >
                          <Tooltip title="Rename">
                            <IconButton
                              size="small"
                              onClick={(e) => handleEditClick(conv, e)}
                              sx={{
                                color: '#8e8ea0',
                                padding: 0.5,
                                '&:hover': {
                                  color: '#ececec',
                                  bgcolor: '#424242',
                                },
                              }}
                            >
                              <EditOutlinedIcon sx={{ fontSize: 16 }} />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Delete">
                            <IconButton
                              size="small"
                              onClick={(e) => handleDeleteClick(conv.id, e)}
                              sx={{
                                color: '#8e8ea0',
                                padding: 0.5,
                                '&:hover': {
                                  color: '#ef4444',
                                  bgcolor: '#424242',
                                },
                              }}
                            >
                              <DeleteOutlineIcon sx={{ fontSize: 16 }} />
                            </IconButton>
                          </Tooltip>
                        </Box>
                      )}
                    </ListItemButton>
                  </ListItem>
                ))}
              </List>
            </>
          )}
        </Box>

        {/* User Section */}
        <Divider sx={{ borderColor: '#2f2f2f' }} />
        <Box sx={{ p: 2 }}>
          {authenticated ? (
            <Box>
              <Box
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1.5,
                  p: 1.5,
                  borderRadius: 2,
                  mb: 1,
                  '&:hover': {
                    bgcolor: '#2f2f2f',
                  },
                }}
              >
                <Box
                  sx={{
                    width: 32,
                    height: 32,
                    borderRadius: '50%',
                    bgcolor: '#5436DA',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Typography sx={{ color: 'white', fontSize: 14, fontWeight: 600 }}>
                    {username.charAt(0).toUpperCase()}
                  </Typography>
                </Box>
                <Box sx={{ flex: 1, minWidth: 0 }}>
                  <Typography
                    variant="body2"
                    sx={{
                      color: '#ececec',
                      fontWeight: 500,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {username}
                  </Typography>
                  {remainingQueries !== null && (
                    <Typography
                      variant="caption"
                      sx={{
                        color: remainingQueries <= 2 ? '#f59e0b' : '#8e8ea0',
                        display: 'block',
                      }}
                    >
                      {remainingQueries} queries left
                    </Typography>
                  )}
                </Box>
              </Box>
              <Button
                fullWidth
                variant="text"
                startIcon={<LogoutIcon />}
                onClick={onLogout}
                sx={{
                  color: '#8e8ea0',
                  justifyContent: 'flex-start',
                  py: 1,
                  px: 1.5,
                  '&:hover': {
                    bgcolor: '#2f2f2f',
                    color: '#ececec',
                  },
                }}
              >
                Log out
              </Button>
            </Box>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Button
                fullWidth
                variant="contained"
                startIcon={<LoginIcon />}
                onClick={onLogin}
                sx={{
                  bgcolor: '#10a37f',
                  color: 'white',
                  py: 1.25,
                  '&:hover': {
                    bgcolor: '#0d8a6c',
                  },
                }}
              >
                Log in
              </Button>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, p: 1 }}>
                <PersonOutlineIcon sx={{ color: '#8e8ea0', fontSize: 20 }} />
                <Typography variant="caption" sx={{ color: '#8e8ea0' }}>
                  Login to save conversations
                </Typography>
              </Box>
            </Box>
          )}
        </Box>
      </Box>

      {/* Edit Dialog */}
      <Dialog 
        open={editingId !== null} 
        onClose={handleCancelEdit}
        PaperProps={{
          sx: {
            bgcolor: '#2f2f2f',
            backgroundImage: 'none',
            borderRadius: 3,
            minWidth: 340,
          }
        }}
      >
        <DialogTitle sx={{ fontWeight: 600, color: '#ececec' }}>
          Rename conversation
        </DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            fullWidth
            value={editTitle}
            onChange={(e) => setEditTitle(e.target.value)}
            onKeyPress={(e) => {
              if (e.key === 'Enter') {
                handleSaveEdit()
              }
            }}
            variant="outlined"
            sx={{
              mt: 1,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#212121',
                borderRadius: 2,
                '& fieldset': {
                  borderColor: '#424242',
                },
                '&:hover fieldset': {
                  borderColor: '#565656',
                },
                '&.Mui-focused fieldset': {
                  borderColor: '#10a37f',
                },
              },
              '& .MuiInputBase-input': {
                color: '#ececec',
              },
            }}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2.5, pt: 1, gap: 1 }}>
          <Button 
            onClick={handleCancelEdit}
            sx={{
              color: '#8e8ea0',
              '&:hover': {
                bgcolor: '#424242',
              },
            }}
          >
            Cancel
          </Button>
          <Button 
            onClick={handleSaveEdit} 
            variant="contained"
            sx={{
              bgcolor: '#10a37f',
              '&:hover': {
                bgcolor: '#0d8a6c',
              },
            }}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Dialog */}
      <Dialog 
        open={deleteDialogOpen} 
        onClose={() => setDeleteDialogOpen(false)}
        PaperProps={{
          sx: {
            bgcolor: '#2f2f2f',
            backgroundImage: 'none',
            borderRadius: 3,
            minWidth: 340,
          }
        }}
      >
        <DialogTitle sx={{ fontWeight: 600, color: '#ececec' }}>
          Delete conversation?
        </DialogTitle>
        <DialogContent>
          <Typography sx={{ color: '#8e8ea0' }}>
            This will delete the conversation and all its messages. This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions sx={{ p: 2.5, pt: 1, gap: 1 }}>
          <Button 
            onClick={() => setDeleteDialogOpen(false)}
            sx={{
              color: '#8e8ea0',
              '&:hover': {
                bgcolor: '#424242',
              },
            }}
          >
            Cancel
          </Button>
          <Button 
            onClick={handleConfirmDelete} 
            variant="contained"
            sx={{
              bgcolor: '#ef4444',
              '&:hover': {
                bgcolor: '#dc2626',
              },
            }}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}
