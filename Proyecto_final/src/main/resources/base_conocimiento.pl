% ============================================================================
% BASE DE CONOCIMIENTO - Sistema experto de integrales y derivadas
% Proyecto Final de Programacion Declarativa, ciclo 02/2026
%
% Organizacion del archivo:
%   1. API para la interfaz Java
%   2. Normalizacion de notacion (alias)
%   3. Reglas de integracion   (casos a .. v del enunciado)
%   4. Reglas de derivacion
%   5. Simplificacion simbolica
%   6. Sustitucion y evaluacion numerica
%   7. Analisis del intervalo: discontinuidades, ceros y area
% ============================================================================

% ============================================================================
% 1. API PARA LA INTERFAZ JAVA
% ============================================================================
% Estos dos predicados son los unicos que Java invoca. Nunca fallan: en lugar
% de fallar devuelven un termino de estado, para que la interfaz pueda
% distinguir "no existe una regla" de "la regla existe pero no se puede
% evaluar en este intervalo". Son dos situaciones muy distintas y el usuario
% merece saber cual le toco.
%
%   integrar(+Expresion, +A, +B, -Resultado)
%     Resultado = ok(Antiderivada, IntegralDefinida, AreaBajoCurva)
%               | error(sin_regla)
%               | error(limite_invalido)
%               | error(fuera_de_dominio(Punto))
%               | error(discontinua(Punto))

integrar(Expresion0, A0, B0, Resultado) :-
    normalizar(Expresion0, Expresion),
    (   integral(Expresion, x, Primitiva0)
    ->  simplificar(Primitiva0, Primitiva),
        analizar(Expresion, Primitiva, A0, B0, Resultado)
    ;   Resultado = error(sin_regla)
    ).

%   derivar(+Expresion, -Resultado)
%     Resultado = ok(Derivada) | error(sin_regla)

derivar(Expresion0, Resultado) :-
    normalizar(Expresion0, Expresion),
    (   derivada(Expresion, x, Derivada0)
    ->  simplificar(Derivada0, Derivada),
        Resultado = ok(Derivada)
    ;   Resultado = error(sin_regla)
    ).

% analizar/5 aplica el teorema fundamental del calculo solo despues de
% comprobar sus hipotesis: que los limites sean numeros, que el integrando y
% la primitiva esten definidos en ambos extremos, y que el integrando no tenga
% discontinuidades dentro del intervalo.
analizar(Expresion, Primitiva, A0, B0, Resultado) :-
    (   \+ valor_num(A0, _)
    ->  Resultado = error(limite_invalido)
    ;   \+ valor_num(B0, _)
    ->  Resultado = error(limite_invalido)
    ;   valor_num(A0, A),
        valor_num(B0, B),
        (   \+ evaluar_en(Expresion, A, _)
        ->  Resultado = error(fuera_de_dominio(A))
        ;   \+ evaluar_en(Expresion, B, _)
        ->  Resultado = error(fuera_de_dominio(B))
        ;   \+ evaluar_en(Primitiva, A, _)
        ->  Resultado = error(fuera_de_dominio(A))
        ;   \+ evaluar_en(Primitiva, B, _)
        ->  Resultado = error(fuera_de_dominio(B))
        ;   discontinuidad(Expresion, A, B, Punto)
        ->  Resultado = error(discontinua(Punto))
        ;   evaluar_en(Primitiva, A, FA),
            evaluar_en(Primitiva, B, FB),
            Definida is FB - FA,
            area_bajo_curva(Expresion, Primitiva, A, B, Area),
            Resultado = ok(Primitiva, Definida, Area)
        )
    ).

% ============================================================================
% 2. NORMALIZACION DE NOTACION
% ============================================================================
% El sistema acepta la notacion hispana habitual, no solo los nombres internos
% de SWI-Prolog. Sin esta capa, quien escribe sen(x) o ln(x) recibe el mismo
% mensaje que quien pide una funcion no implementada, y desde fuera parece que
% el caso no existe.

normalizar(T, T) :- var(T), !.
normalizar(N, N) :- number(N), !.
normalizar(A, A) :- atomic(A), !.
normalizar(T, R) :-
    compound(T),
    T =.. [Functor | Args],
    normalizar_lista(Args, ArgsN),
    T1 =.. [Functor | ArgsN],
    (   alias(T1, R0)
    ->  R = R0
    ;   R = T1
    ).

normalizar_lista([], []).
normalizar_lista([H|T], [HN|TN]) :-
    normalizar(H, HN),
    normalizar_lista(T, TN).

% Trigonometricas
alias(sen(X),       sin(X)).
alias(seno(X),      sin(X)).
alias(coseno(X),    cos(X)).
alias(tg(X),        tan(X)).
alias(tang(X),      tan(X)).
% Trigonometricas inversas
alias(arcsen(X),    asin(X)).
alias(arcsin(X),    asin(X)).
alias(arccos(X),    acos(X)).
alias(arctan(X),    atan(X)).
alias(arctg(X),     atan(X)).
% Hiperbolicas
alias(sh(X),        sinh(X)).
alias(ch(X),        cosh(X)).
alias(th(X),        tanh(X)).
alias(senh(X),      sinh(X)).
% Hiperbolicas inversas
alias(arcsinh(X),   asinh(X)).
alias(arcsenh(X),   asinh(X)).
alias(arccosh(X),   acosh(X)).
alias(arctanh(X),   atanh(X)).
% Exponencial y logaritmo
alias(ln(X),        log(X)).
alias(e^X,          exp(X)).

% ============================================================================
% 3. REGLAS DE INTEGRACION
% ============================================================================
% Cada regla lleva la letra del caso del enunciado que resuelve.
% El orden importa: las formas concretas van antes que las generales, para que
% cada entrada tenga una sola solucion y la mas legible.

% --- a) integral de k dx, con k constante numerica
integral(K, x, K*x) :-
    number(K).

% --- b) integral de x dx
integral(x, x, (x^2)/2).

% --- d) integral de x^n dx, con n entero
%     El caso n = -1 va aparte porque la regla de la potencia no aplica.
integral(x^(-1), x, log(abs(x))).
integral(1/x,    x, log(abs(x))).
integral(x^N, x, (x^N1)/N1) :-
    number(N),
    N =\= -1,
    N1 is N + 1.

% --- k) l) m) integral de las trigonometricas
integral(sin(x), x, -cos(x)).
integral(cos(x), x, sin(x)).
integral(tan(x), x, -log(abs(cos(x)))).

% --- n) o) p) integral de las trigonometricas inversas
integral(asin(x), x, x*asin(x) + sqrt(1-x^2)).
integral(acos(x), x, x*acos(x) - sqrt(1-x^2)).
integral(atan(x), x, x*atan(x) - log(1+x^2)/2).

% --- q) r) s) integral de las hiperbolicas
integral(sinh(x), x, cosh(x)).
integral(cosh(x), x, sinh(x)).
integral(tanh(x), x, log(cosh(x))).

% --- t) u) v) integral de las hiperbolicas inversas
integral(asinh(x), x, x*asinh(x) - sqrt(x^2+1)).
integral(acosh(x), x, x*acosh(x) - sqrt(x^2-1)).
integral(atanh(x), x, x*atanh(x) + log(1-x^2)/2).

% --- exponencial natural
integral(exp(x), x, exp(x)).

% --- g) h) integral de (u + v) y de (u - v)
integral(U + V, x, IntU + IntV) :-
    integral(U, x, IntU),
    integral(V, x, IntV).
integral(U - V, x, IntU - IntV) :-
    integral(U, x, IntU),
    integral(V, x, IntV).

% --- menos unario: integral de -u dx
integral(-U, x, -IntU) :-
    integral(U, x, IntU).

% --- f) integral de (k * u) dx, con k constante y u cualquier funcion de x
%     Una sola regla cubre 3*x, 2*x^3, 3*tan(x), 2*exp(x), 5*cosh(x) y
%     cualquier otra forma que el sistema sepa integrar. Las tres variantes
%     aceptan la constante a izquierda, a derecha, y como divisor.
integral(K*U, x, K*IntU) :-
    number(K),
    \+ number(U),
    integral(U, x, IntU).
integral(U*K, x, K*IntU) :-
    number(K),
    \+ number(U),
    integral(U, x, IntU).
integral(U/K, x, IntU/K) :-
    number(K),
    K =\= 0,
    integral(U, x, IntU).

% --- e) integral de u^n dx, con u funcion de x
%     La regla de la potencia solo aplica cuando du es constante, porque
%     entonces dx = du/M y la integral es u^(n+1)/((n+1)*M). Cuando du
%     depende de x (por ejemplo sin(x)^2) la regla no vale y el sistema
%     falla honestamente: resolverla exigiria identidades trigonometricas
%     que el enunciado no pide. Es la misma tecnica del caso j) de abajo.
integral(U^N, x, (U^N1)/(N1*M)) :-
    number(N),
    N =\= -1,
    \+ number(U),
    U \== x,
    derivada(U, x, DU),
    \+ sub_term(x, DU),
    M is DU,
    M =\= 0,
    N1 is N + 1.

% --- i) j) integral de a^x dx y de a^u dx, con a constante
integral(A^U, x, Resultado) :-
    number(A),
    A > 0,
    (   A =:= 1
    ->  Resultado = x
    ;   derivada(U, x, DU),
        \+ sub_term(x, DU),
        M is DU,
        (   M =:= 0
        ->  Resultado = A^U*x
        ;   Resultado = A^U/(M*log(A))
        )
    ).

% ============================================================================
% 4. REGLAS DE DERIVACION
% ============================================================================
% La derivacion no la pide el enunciado, pero el sistema la ofrece y las
% reglas de los casos e) y j) la usan internamente para obtener du.

derivada(K, x, 0) :- number(K).
derivada(x, x, 1).

derivada(K*x, x, K) :- number(K).
derivada(x^N, x, N*x^(N-1)) :- number(N).

derivada(U + V, x, DU + DV) :-
    derivada(U, x, DU),
    derivada(V, x, DV).
derivada(U - V, x, DU - DV) :-
    derivada(U, x, DU),
    derivada(V, x, DV).
derivada(-U, x, -DU) :-
    derivada(U, x, DU).

% Funciones elementales
derivada(sin(x),   x, cos(x)).
derivada(cos(x),   x, -sin(x)).
derivada(tan(x),   x, 1/(cos(x)^2)).
derivada(exp(x),   x, exp(x)).
derivada(log(x),   x, 1/x).
derivada(asin(x),  x, 1/sqrt(1-x^2)).
derivada(acos(x),  x, -1/sqrt(1-x^2)).
derivada(atan(x),  x, 1/(1+x^2)).
derivada(sinh(x),  x, cosh(x)).
derivada(cosh(x),  x, sinh(x)).
derivada(tanh(x),  x, 1/(cosh(x)^2)).
derivada(asinh(x), x, 1/sqrt(x^2+1)).
derivada(acosh(x), x, 1/sqrt(x^2-1)).
derivada(atanh(x), x, 1/(1-x^2)).

% Regla del producto y del cociente
derivada(U*V, x, DU*V + U*DV) :-
    derivada(U, x, DU),
    derivada(V, x, DV).
derivada(U/K, x, DU/K) :-
    number(K),
    derivada(U, x, DU).
derivada(U/V, x, (DU*V - U*DV)/V^2) :-
    derivada(U, x, DU),
    derivada(V, x, DV).

% Regla de la cadena, para las funciones de la tabla aplicadas a u(x)
derivada(sin(U),   x, cos(U)*DU)            :- U \== x, derivada(U, x, DU).
derivada(cos(U),   x, -sin(U)*DU)           :- U \== x, derivada(U, x, DU).
derivada(tan(U),   x, DU/(cos(U)^2))        :- U \== x, derivada(U, x, DU).
derivada(exp(U),   x, exp(U)*DU)            :- U \== x, derivada(U, x, DU).
derivada(log(U),   x, DU/U)                 :- U \== x, derivada(U, x, DU).
derivada(sinh(U),  x, cosh(U)*DU)           :- U \== x, derivada(U, x, DU).
derivada(cosh(U),  x, sinh(U)*DU)           :- U \== x, derivada(U, x, DU).
derivada(tanh(U),  x, DU/(cosh(U)^2))       :- U \== x, derivada(U, x, DU).
derivada(U^N,      x, N*U^(N-1)*DU)         :- number(N), U \== x, derivada(U, x, DU).
derivada(A^U,      x, A^U*log(A)*DU)        :- number(A), derivada(U, x, DU).

% ============================================================================
% 5. SIMPLIFICACION SIMBOLICA
% ============================================================================
% No es un algebra computacional completa: solo quita el ruido que generan las
% reglas de arriba, para que el usuario no lea cosas como x^3/3- -cos(x) o
% 2^x/(1*log(2)). Aplica reglas hasta que el termino deja de cambiar.

simplificar(T, R) :-
    paso_simplificacion(T, T1),
    T1 \== T,
    !,
    simplificar(T1, R).
simplificar(T, T).

% Un paso simplifica primero los subterminos y luego la raiz.
paso_simplificacion(T, T) :- var(T), !.
paso_simplificacion(N, N) :- number(N), !.
paso_simplificacion(A, A) :- atomic(A), !.
paso_simplificacion(T, R) :-
    compound(T),
    T =.. [Functor | Args],
    paso_lista(Args, ArgsS),
    T1 =.. [Functor | ArgsS],
    (   regla_simple(T1, R0)
    ->  R = R0
    ;   R = T1
    ).

paso_lista([], []).
paso_lista([H|T], [HS|TS]) :-
    paso_simplificacion(H, HS),
    paso_lista(T, TS).

% Signos
regla_simple(A - -B, A + B).
regla_simple(A + -B, A - B).
regla_simple(- -A,   A).
regla_simple(0 - A,  -A).
% Neutros y absorbentes
regla_simple(1*A,    A).
regla_simple(A*1,    A).
regla_simple(A/1,    A).
regla_simple(0 + A,  A).
regla_simple(A + 0,  A).
regla_simple(A - 0,  A).
regla_simple(0*_,    0).
regla_simple(_*0,    0).
regla_simple(A^1,    A).
% Reacomodo de constantes, para que k*(u/n) se lea como k*u/n
regla_simple(A * -B, -(A*B)).
regla_simple(K*(A/B), K*A/B) :- number(K), number(B).
regla_simple(K1*A/K2, A)     :- number(K1), number(K2), K1 =:= K2.
% Plegado de constantes
regla_simple(A + B, C) :- number(A), number(B), C is A + B.
regla_simple(A - B, C) :- number(A), number(B), C is A - B.
regla_simple(A * B, C) :- number(A), number(B), C is A * B.
regla_simple(A/K1/K2, A/K) :- number(K1), number(K2), K is K1 * K2.

% ============================================================================
% 6. SUSTITUCION Y EVALUACION NUMERICA
% ============================================================================

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

% valor_num/2 y evaluar_en/3 nunca lanzan excepciones: un limite fuera del
% dominio, una division por cero o una raiz de numero negativo simplemente
% hacen fallar el predicado. Asi la excepcion de Prolog no viaja por JPL
% hasta la ventana de Java como un volcado ilegible.
valor_num(T, V) :-
    catch(V is T, _, fail),
    number(V),
    V =:= V.                      % descarta NaN

evaluar_en(Expr, Valor, Resultado) :-
    sustituir(x, Valor, Expr, ExprSustituida),
    valor_num(ExprSustituida, Resultado).

% ============================================================================
% 7. ANALISIS DEL INTERVALO
% ============================================================================

% --- Discontinuidades -------------------------------------------------------
% El teorema fundamental del calculo exige que f sea continua en [a,b]. Sin
% esta comprobacion el sistema contestaria que la integral de x^(-2) entre -1
% y 1 vale -2, cuando en realidad diverge por el polo en x = 0.
%
% Se usan dos mecanismos complementarios:
%   a) una malla sobre el intervalo, que detecta los puntos donde f no esta
%      definida o donde su magnitud ya es enorme (polos de orden par, como
%      x^(-2), que no cambian de signo);
%   b) el refinamiento de los cambios de signo, que distingue un cero
%      verdadero de un polo de orden impar (como tan(x) en pi/2).

discontinuidad(Expr, A, B, Punto) :-
    punto_indefinido(Expr, A, B, Punto),
    !.
discontinuidad(Expr, A, B, Punto) :-
    cruce(Expr, A, B, polo(Punto)),
    !.

punto_indefinido(Expr, A, B, Punto) :-
    malla(A, B, T),
    (   \+ evaluar_en(Expr, T, _)
    ->  Punto = T
    ;   evaluar_en(Expr, T, V),
        abs(V) > 1.0e10,
        Punto = T
    ).

malla(A, B, T) :-
    pasos(N),
    between(0, N, I),
    T is A + (B - A) * I / N.

pasos(500).

% --- Ceros y polos ----------------------------------------------------------
% cruce/4 recorre la malla buscando cambios de signo de f y refina cada uno
% por biseccion. Si al refinar la magnitud crece sin control, el cambio de
% signo no era un cero sino un polo.

% Importante: refinar/5 se llama siempre con la salida sin instanciar y el
% resultado se unifica despues. Si se le pasara el patron ya instanciado
% (por ejemplo polo(P)), el caso base no podria unificar y la recursion no
% terminaria nunca.
% Primero los ceros que caen justo sobre un punto de la malla: sin esta
% clausula se perderia el cero de x en [-2,2], porque ahi no hay cambio de
% signo entre dos puntos consecutivos sino un cero exacto en uno de ellos.
cruce(Expr, A, B, cero(T)) :-
    malla(A, B, T),
    evaluar_en(Expr, T, V),
    V =:= 0.
cruce(Expr, A, B, Tipo) :-
    pasos(N),
    N1 is N - 1,
    between(0, N1, I),
    T0 is A + (B - A) * I / N,
    T1 is A + (B - A) * (I + 1) / N,
    evaluar_en(Expr, T0, V0),
    evaluar_en(Expr, T1, V1),
    V0 * V1 < 0,
    refinar(Expr, T0, T1, 0, Resultado),
    Resultado = Tipo.

profundidad_maxima(60).

refinar(Expr, T0, T1, D, Tipo) :-
    profundidad_maxima(Max),
    (   D >= Max
    ->  Z is (T0 + T1) / 2,
        Tipo = cero(Z)
    ;   M is (T0 + T1) / 2,
        (   \+ evaluar_en(Expr, M, _)
        ->  Tipo = polo(M)
        ;   evaluar_en(Expr, M, VM),
            (   abs(VM) > 1.0e10
            ->  Tipo = polo(M)
            ;   VM =:= 0
            ->  Tipo = cero(M)
            ;   evaluar_en(Expr, T0, V0),
                D1 is D + 1,
                (   V0 * VM < 0
                ->  refinar(Expr, T0, M, D1, Tipo)
                ;   refinar(Expr, M, T1, D1, Tipo)
                )
            )
        )
    ).

% --- Area bajo la curva -----------------------------------------------------
% El enunciado pide el area bajo la curva, que no es lo mismo que la integral
% definida: la integral de sin(x) entre 0 y 2*pi vale 0, pero el area encerrada
% es 4. Se parte el intervalo en los ceros de f y se suman los valores
% absolutos de los incrementos de la primitiva en cada tramo, usando la
% primitiva exacta que ya se calculo.

area_bajo_curva(Expr, Primitiva, A, B, Area) :-
    findall(Z, (cruce(Expr, A, B, cero(Z))), Ceros0),
    sort(Ceros0, Ceros),
    append([A | Ceros], [B], Puntos),
    sumar_tramos(Primitiva, Puntos, 0, Area).

sumar_tramos(_, [_], Acumulado, Acumulado) :- !.
sumar_tramos(Primitiva, [P, Q | Resto], Acumulado, Area) :-
    evaluar_en(Primitiva, P, FP),
    evaluar_en(Primitiva, Q, FQ),
    Tramo is abs(FQ - FP),
    Acumulado1 is Acumulado + Tramo,
    sumar_tramos(Primitiva, [Q | Resto], Acumulado1, Area).

% ============================================================================
% COMPATIBILIDAD
% ============================================================================
% Se conservan los predicados de la version anterior para que cualquier
% consulta manual escrita durante el desarrollo siga funcionando.

resolver_integral(Expresion, A, B, Primitiva, Definida) :-
    integrar(Expresion, A, B, ok(Primitiva, Definida, _)).

resolver_derivada(Expresion, Derivada) :-
    derivar(Expresion, ok(Derivada)).
