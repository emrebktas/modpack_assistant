import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Typography,
  Alert,
  IconButton,
  InputAdornment,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { login } from '../services/authService';

interface LoginDialogProps {
  open: boolean;
  onClose: () => void;
  onLoginSuccess: (username: string) => void;
  onSwitchToRegister: () => void;
}

export default function LoginDialog({ open, onClose, onLoginSuccess, onSwitchToRegister }: LoginDialogProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleLogin = async () => {
    if (!username.trim() || !password.trim()) {
      setError('Please enter both username and password');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await login({ username, password });
      onLoginSuccess(response.username);
      onClose();
      setUsername('');
      setPassword('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleLogin();
    }
  };

  const handleClose = () => {
    setError('');
    onClose();
  };

  return (
    <Dialog 
      open={open} 
      onClose={handleClose}
      maxWidth="xs"
      fullWidth
      PaperProps={{
        sx: {
          bgcolor: '#2f2f2f',
          backgroundImage: 'none',
          borderRadius: 3,
        }
      }}
    >
      <DialogTitle sx={{ 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'space-between',
        pb: 1,
      }}>
        <Typography variant="h6" sx={{ fontWeight: 600, color: '#ececec' }}>
          Welcome back
        </Typography>
        <IconButton 
          onClick={handleClose} 
          size="small"
          sx={{
            color: '#8e8ea0',
            '&:hover': {
              bgcolor: '#424242',
            },
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, pt: 1 }}>
          {error && (
            <Alert 
              severity="error" 
              onClose={() => setError('')}
              sx={{
                bgcolor: 'rgba(239, 68, 68, 0.1)',
                color: '#ef4444',
                '& .MuiAlert-icon': {
                  color: '#ef4444',
                },
              }}
            >
              {error}
            </Alert>
          )}

          <TextField
            label="Username"
            variant="outlined"
            fullWidth
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={loading}
            autoFocus
            sx={{
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
              '& .MuiInputLabel-root': {
                color: '#8e8ea0',
                '&.Mui-focused': {
                  color: '#10a37f',
                },
              },
            }}
          />

          <TextField
            label="Password"
            type={showPassword ? 'text' : 'password'}
            variant="outlined"
            fullWidth
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={loading}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    onClick={() => setShowPassword(!showPassword)}
                    edge="end"
                    sx={{ color: '#8e8ea0' }}
                  >
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
            sx={{
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
              '& .MuiInputLabel-root': {
                color: '#8e8ea0',
                '&.Mui-focused': {
                  color: '#10a37f',
                },
              },
            }}
          />

          <Typography variant="body2" sx={{ color: '#8e8ea0' }} textAlign="center">
            Don't have an account?{' '}
            <Button
              variant="text"
              size="small"
              onClick={onSwitchToRegister}
              sx={{ 
                textTransform: 'none', 
                p: 0, 
                minWidth: 'auto',
                color: '#10a37f',
                fontWeight: 500,
                '&:hover': {
                  bgcolor: 'transparent',
                  textDecoration: 'underline',
                },
              }}
            >
              Sign up
            </Button>
          </Typography>
        </Box>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 3, gap: 1.5 }}>
        <Button 
          onClick={handleClose} 
          disabled={loading}
          sx={{
            color: '#8e8ea0',
            px: 3,
            '&:hover': {
              bgcolor: '#424242',
            },
          }}
        >
          Cancel
        </Button>
        <Button
          variant="contained"
          onClick={handleLogin}
          disabled={loading || !username.trim() || !password.trim()}
          sx={{
            bgcolor: '#10a37f',
            px: 3,
            '&:hover': {
              bgcolor: '#0d8a6c',
            },
            '&.Mui-disabled': {
              bgcolor: '#424242',
              color: '#8e8ea0',
            },
          }}
        >
          {loading ? 'Logging in...' : 'Continue'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
