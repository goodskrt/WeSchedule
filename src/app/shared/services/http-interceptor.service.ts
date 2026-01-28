import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

import { TokenService } from './token.service';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private tokenService: TokenService,
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Ajouter le token JWT aux requêtes si disponible
    const token = this.tokenService.getToken();
    
    if (token && this.tokenService.isValidToken()) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // Gérer les erreurs d'authentification
        if (error.status === 401) {
          // Token expiré ou invalide
          this.authService.logout();
          this.router.navigate(['/connexion']);
        } else if (error.status === 403) {
          // Accès refusé
          console.error('Accès refusé:', error);
          this.router.navigate(['/app/dashboard']);
        }
        
        return throwError(() => error);
      })
    );
  }
}