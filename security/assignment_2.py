import math

def addition(a, b, B):
    a = a[::-1]
    b = b[::-1]
    k = len(a)
    l = len(b)
    c = [0] * (k+1)
    carry = 0

    for i in range(l):
        tmp = a[i] + b[i] + carry
        carry = tmp // B
        c[i] = tmp % B

    for i in range(l, k):
        tmp = a[i] + carry
        carry = tmp // B
        c[i] = tmp % B

    c[k] = carry
    return int("".join(map(str, c[::-1])))

def fibonacci(n):
    B = 10
    u0 = 0
    u1 = 1
    result = [u0, u1]

    for i in range(n-1):
        x = addition([u0], [u1], B)
        result.append(x)
        u0 = u1
        u1 = x

    return result[-1]

def multiplication(a, b, B):
    a = a[::-1]
    b = b[::-1]
    k = len(a)
    l = len(b)
    c = [0] * (k + l)
    carry = 0

    for i in range(k):
        carry = 0
        for j in range(l):
            tmp = a[i] * b[j] + c[i+j] + carry
            carry = tmp // B
            c[i+j] = tmp % B

        c[i+l] = carry

    return int("".join(map(str, c[::-1])))

def factorial(n):
    if n == 0:
        return 1
    else:
        result = 1
        for i in range(1, n+1):
            result = multiplication([r], [i], B)

        return result

def expmod(a, b, m):
    c = 1
    b = bin(b)[2:]

    for i in b:
        c = (c**2) % m 
        if i == '1':
            c = (c * a) % m

    return c

if __name__ == "__main__":
    B = 10
    print(addition([6, 4, 7], [8, 5], B))
    print(multiplication([3, 7], [8, 5], B))
    print(fibonacci(101))
    print(factorial(30))
    #print(expmod(2342, 6762, 9343))
