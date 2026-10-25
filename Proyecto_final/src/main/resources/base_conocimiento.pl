% Reglas de integración
integral(K, x, K*x) :- number(K).
integral(x, x, (x^2)/2).
integral(K*x, x, K*(x^2)/2) :- number(K).
integral(x^N, x, (x^N1)/N1) :- number(N), N \= -1, N1 is N+1.
integral(U + V, x, IntU + IntV) :- integral(U, x, IntU), integral(V, x, IntV).
integral(U - V, x, IntU - IntV) :- integral(U, x, IntU), integral(V, x, IntV).
integral(sin(x), x, -cos(x)).
integral(cos(x), x, sin(x)).
integral(tan(x), x, -log(cos(x))).
integral(A*sin(x), x, -A*cos(x)) :- number(A).
integral(A*cos(x), x, A*sin(x)) :- number(A).
integral(cosh(x), x, sinh(x)).

% Evaluación y cálculo de área
evaluar_en(Expr, x, Limite, Resultado) :-
    copy_term(Expr-x, CopiaExpr-CopiaX),
    CopiaX = Limite,
    Resultado is CopiaExpr.

resolver_integral(Expresion, A, B, ExprIntegrada, Area) :-
    integral(Expresion, x, ExprIntegrada),
    evaluar_en(ExprIntegrada, x, A, AreaA),
    evaluar_en(ExprIntegrada, x, B, AreaB),
    Area is AreaB - AreaA.