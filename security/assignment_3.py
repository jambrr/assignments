from sage.all import *
from sage.misc.prandom import randrange

#Fermat primality test
def fermat(n, t):
    for i in range(t+1):
        a = randrange(2, n)
        r = pow(a, n-1, n)

        if r != 1:
            return "composite"
    return "prime"

def prime_gen(k):
    while True:
        p = randint(2**(k-1), 2**k-1)

        p = p | 2**(k-1)
        p = p | 2**(k-2)

        if is_prime(p):
            return p

def key_gen(k):
    p = prime_gen(k/2)
    q = prime_gen(k/2)
    
    n = p * q
    sigma = (p-1)*(q-1)
    
    e = -1
    while e < 0:
        x = randrange(sigma)
        if gcd(x) == 1:
            e = x

    
#Encrypt the message
def encryption(e, m, n):
    return (m**d) % n

#Decrypt the message
def decryption(d, c, n):
    return (c**d) % n

if __name__ == "__main__":
    print(fermat(11, 7))
    print(prime_gen(16))
    #print(key_gen())
    
