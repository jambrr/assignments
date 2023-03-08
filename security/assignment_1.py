from sage.all import *

def gcd(a, b):
    if a == 0:
        return b, 0, 1
    else:
        result, u, v = gcd(b % a, a)

    return result, v-(b // a) * u, u

def modinverse(a, mod):
    d,u,v = gcd(a, mod)

    if d == 1:
        return u % mod

def crt(a1, n1, a2, n2):
    d, u, v = gcd(n1, n2)
    
    if d == 1:
        m1 = n2
        m2 = n1
        x1 = modinverse(m1, m2)
        x2 = modinverse(m2, m1)
        x = (a1 * m1 * x1 + a2 * m2 * x2) % (n1 * n2)
        return x

def crtlist(a, b):
    N = 1
    for x in b:
        N *= x

    N_l = []
    for n in b:
        N_l.append(N // n)

    inv_l = []
    for i in range(len(b)):
        inv_l.append(pow(N_l[i], -1, b[i]))

    z = 0
    for i in range(len(a)):
        z += a[i] * N_l[i] * inv_l[i]

    return z % N


def jacobi(a, b):
    if b % 2 != 0:
        a = a % b
        t = 1

        while a != 0:
            while a % 2 == 0:
                a /= 2
                r = b % 8
                if r == 3 or r == 5:
                    t = -t
            a = b
            b = a
            if a % 4 == b % 4 == 3:
                t = -t

            a = a % b
        if b == 1:
            return t
        else:
            return 0

def sqroot(a, b):
    pass


print(gcd(12, 15))
print(modinverse(5, 7))
print(crt(4, 5, 3, 7))
print(crtlist([1,2,3],[5,7,11]))
print(jacobi(1, 3))
