export interface LoginRequest {
  email: string;
  motDePasse: string;
}

export interface AuthResponse {
  idUser: string;
  email: string;
  nom: string;
  prenom: string;
  role: 'ADMINISTRATEUR' | 'ENSEIGNANT' | 'ETUDIANT';
  message: string;
  success: boolean;
  token?: string; // Pour l'authentification JWT
  phone?: string; // Téléphone (pour les enseignants)
  grade?: string; // Grade (pour les enseignants)
}

export interface User {
  idUser: string;
  email: string;
  nom: string;
  prenom: string;
  role: 'ADMINISTRATEUR' | 'ENSEIGNANT' | 'ETUDIANT';
  phone?: string;
  grade?: string; // Pour les enseignants
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  phone?: string;
  motDePasse: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}