import math

def addition(a, b, B):
    k = len(a)
    l = len(b)
    c = [0] * (k+1)
    carry = 0

    for i in range(l):
        print(i)
        tmp = a[i] + b[i] + carry
        carry = tmp // B
        c[i] = tmp % B

    print('test')
    for i in range(l, k):
        print(i)
        tmp = a[i] + carry
        carry = tmp // B
        c[i] = tmp % B

    c[k] = carry
    return c

def fibonacci():
    pass

def multiplication(a, b, B):
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

    print(tmp)
    print(carry)

    return c

def factorial():
    pass

def expmod(a, b, m):
    pass


if __name__ == "__main__":
    B = 10
    print(addition([6, 4, 7], [8, 5], B))
    print(multiplication([3, 7], [8, 5], B))
