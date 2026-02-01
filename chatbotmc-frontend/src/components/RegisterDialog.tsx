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
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { register } from '../services/authService';

interface RegisterDialogProps {
  open: boolean;
  onClose: () => void;
  onRegisterSuccess: (username: string) => void;
  onSwitchToLogin: () => void;
}

export default function RegisterDialog({ open, onClose, onRegisterSuccess, onSwitchToLogin }: RegisterDialogProps) {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleRegister = async () => {
    if (!username.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()) {
      setError('Please fill in all fields');
      return;
    }

    if (username.length < 3 || username.length > 20) {
      setError('Username must be between 3 and 20 characters');
      return;
    }

    if (password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (!email.includes('@')) {
      setError('Please enter a valid email address');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await register({ username, email, password });
      setSuccess(true);
      onRegisterSuccess(response.username);
      setUsername('');
      setEmail('');
      setPassword('');
      setConfirmPassword('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleRegister();
    }
  };

  const handleClose = () => {
    setError('');
    setSuccess(false);
    onClose();
  };

  const inputStyles = {
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
    '& .MuiFormHelperText-root': {
      color: '#8e8ea0',
    },
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
          Create your account
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
        {success ? (
          <Box sx={{ py: 3, textAlign: 'center' }}>
            <CheckCircleOutlineIcon sx={{ fontSize: 64, color: '#10a37f', mb: 2 }} />
            <Typography variant="h6" sx={{ color: '#ececec', mb: 1, fontWeight: 600 }}>
              Registration successful!
            </Typography>
            <Typography sx={{ color: '#8e8ea0', mb: 3 }}>
              Your account is pending admin approval. You'll receive an email once approved.
            </Typography>
            <Button
              variant="contained"
              onClick={handleClose}
              fullWidth
              sx={{
                bgcolor: '#10a37f',
                py: 1.5,
                '&:hover': {
                  bgcolor: '#0d8a6c',
                },
              }}
            >
              Got it
            </Button>
          </Box>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
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
              helperText="3-20 characters"
              sx={inputStyles}
            />

            <TextField
              label="Email"
              type="email"
              variant="outlined"
              fullWidth
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onKeyPress={handleKeyPress}
              disabled={loading}
              sx={inputStyles}
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
              helperText="At least 8 characters"
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
              sx={inputStyles}
            />

            <TextField
              label="Confirm password"
              type={showConfirmPassword ? 'text' : 'password'}
              variant="outlined"
              fullWidth
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              onKeyPress={handleKeyPress}
              disabled={loading}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      edge="end"
                      sx={{ color: '#8e8ea0' }}
                    >
                      {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
              sx={inputStyles}
            />

            <Typography variant="body2" sx={{ color: '#8e8ea0' }} textAlign="center">
              Already have an account?{' '}
              <Button
                variant="text"
                size="small"
                onClick={onSwitchToLogin}
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
                Log in
              </Button>
            </Typography>
          </Box>
        )}
      </DialogContent>

      {!success && (
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
            onClick={handleRegister}
            disabled={loading || !username.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()}
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
            {loading ? 'Creating account...' : 'Continue'}
          </Button>
        </DialogActions>
      )}
    </Dialog>
  );
}
