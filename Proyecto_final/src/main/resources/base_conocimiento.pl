% Base de conocimiento para la calculadora de integrales y derivadas.
% Sintaxis de entrada: tan(x), asin(x), acos(x), atan(x), sh(x),
% cosh(x), tanh(x), arcsinh(x), arccosh(x), arctanh(x).

% =========================
% REGLAS DE INTEGRACION
% =========================
integral(K, x, K*x) :- number(K).
integral(x, x, (x^2)/2).
integral(K*x, x, K*(x^2)/2) :- number(K).
integral(x^N, x, (x^N1)/N1) :- number(N), N =\= -1, N1 is N+1.
integral(K*x^N, x, K*(x^N1)/N1) :- number(K), number(N), N =\= -1, N1 is N+1.
integral(U + V, x, IntU + IntV) :- integral(U, x, IntU), integral(V, x, IntV).
integral(U - V, x, IntU - IntV) :- integral(U, x, IntU), integral(V, x, IntV).
integral(sin(x), x, -cos(x)).
integral(cos(x), x, sin(x)).
integral(K*sin(x), x, -K*cos(x)) :- number(K).
integral(K*cos(x), x, K*sin(x)) :- number(K).
integral(tan(x), x, -log(abs(cos(x)))).
integral(asin(x), x, x*asin(x) + sqrt(1-x^2)).
integral(acos(x), x, x*acos(x) - sqrt(1-x^2)).
integral(atan(x), x, x*atan(x) - log(1+x^2)/2).
integral(sh(x), x, cosh(x)).
integral(sinh(x), x, cosh(x)).
integral(cosh(x), x, sinh(x)).
integral(tanh(x), x, log(cosh(x))).
integral(arcsinh(x), x, x*asinh(x) - sqrt(x^2+1)).
integral(asinh(x), x, x*asinh(x) - sqrt(x^2+1)).
integral(arccosh(x), x, x*acosh(x) - sqrt(x^2-1)).
integral(acosh(x), x, x*acosh(x) - sqrt(x^2-1)).
integral(arctanh(x), x, x*atanh(x) + log(1-x^2)/2).
integral(atanh(x), x, x*atanh(x) + log(1-x^2)/2).
integral(exp(x), x, exp(x)).
integral(A^U, x, Resultado) :-
    number(A), A > 0,
    ( A =:= 1 ->
        Resultado = x
    ; derivada(U, x, DU),
      \+ sub_term(x, DU),
      M is DU,
      ( M =:= 0 ->
          Resultado = A^U*x
      ; Resultado = A^U/(M*log(A))
      )
    ).

% =========================
% REGLAS DE DERIVACION
% =========================
derivada(K, x, 0) :- number(K).
derivada(x, x, 1).
derivada(K*x, x, K) :- number(K).
derivada(x^N, x, N*x^(N-1)) :- number(N).
derivada(U + V, x, DU + DV) :- derivada(U, x, DU), derivada(V, x, DV).
derivada(U - V, x, DU - DV) :- derivada(U, x, DU), derivada(V, x, DV).
derivada(sin(x), x, cos(x)).
derivada(cos(x), x, -sin(x)).
derivada(tan(x), x, 1/(cos(x)^2)).
derivada(exp(x), x, exp(x)).
derivada(asin(x), x, 1/sqrt(1-x^2)).
derivada(acos(x), x, -1/sqrt(1-x^2)).
derivada(atan(x), x, 1/(1+x^2)).
derivada(sh(x), x, cosh(x)).
derivada(sinh(x), x, cosh(x)).
derivada(cosh(x), x, sinh(x)).
derivada(tanh(x), x, 1/(cosh(x)^2)).
derivada(arcsinh(x), x, 1/sqrt(x^2+1)).
derivada(asinh(x), x, 1/sqrt(x^2+1)).
derivada(arccosh(x), x, 1/sqrt(x^2-1)).
derivada(acosh(x), x, 1/sqrt(x^2-1)).
derivada(arctanh(x), x, 1/(1-x^2)).
derivada(atanh(x), x, 1/(1-x^2)).

resolver_derivada(Expresion, Resultado) :-
    derivada(Expresion, x, Resultado).

% =========================
% SUSTITUCION Y EVALUACION
% =========================
sustituir(X, V, X, V) :- !.
sustituir(_, _, N, N) :- number(N), !.
sustituir(_, _, A, A) :- atomic(A), !.
sustituir(X, V, Termino, TerminoSustituido) :-
    compound(Termino),
    Termino =.. [Op | Args],
    sustituir_lista(X, V, Args, ArgsSustituidos),
    TerminoSustituido =.. [Op | ArgsSustituidos].

sustituir_lista(_, _, [], []).
sustituir_lista(X, V, [H|T], [HR|TR]) :-
    sustituir(X, V, H, HR),
    sustituir_lista(X, V, T, TR).

evaluar_en(Expr, Limite, Resultado) :-
    sustituir(x, Limite, Expr, ExprSustituida),
    Resultado is ExprSustituida.

resolver_integral(Expresion, A, B, ExprIntegrada, Area) :-
    integral(Expresion, x, ExprIntegrada),
    evaluar_en(ExprIntegrada, A, AreaA),
    evaluar_en(ExprIntegrada, B, AreaB),
    Area is AreaB - AreaA.
