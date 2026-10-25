% Reglas de integración (12 casos según requerimientos del proyecto)
integral(K, x, K*x) :- number(K).
integral(x, x, (x^2)/2).
integral(K*x, x, K*(x^2)/2) :- number(K).
integral(x^N, x, (x^N1)/N1) :- number(N), N \= -1, N1 is N+1.
integral(K*x^N, x, K*(x^N1)/N1) :- number(K), number(N), N \= -1, N1 is N+1.
integral(U + V, x, IntU + IntV) :- integral(U, x, IntU), integral(V, x, IntV).
integral(U - V, x, IntU - IntV) :- integral(U, x, IntU), integral(V, x, IntV).
integral(sin(x), x, -cos(x)).
integral(cos(x), x, sin(x)).
integral(K*sin(x), x, -K*cos(x)) :- number(K).
integral(K*cos(x), x, K*sin(x)) :- number(K).
integral(exp(x), x, exp(x)).

% Algoritmo de sustitución (reemplaza el átomo 'x' por el límite numérico)
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

% Evaluación de área y límites
evaluar_en(Expr, Limite, Resultado) :-
    sustituir(x, Limite, Expr, ExprSustituida),
    Resultado is ExprSustituida.

resolver_integral(Expresion, A, B, ExprIntegrada, Area) :-
    integral(Expresion, x, ExprIntegrada),
    evaluar_en(ExprIntegrada, A, AreaA),
    evaluar_en(ExprIntegrada, B, AreaB),
    Area is AreaB - AreaA.