from sage.all import *
from sage.misc.prandom import randrange

#Fermat primality test
def fermat(n, t):
    for i in range(t):
        a = randint(2, n-2)
        r = pow(a, n-1, n)

        if r != 1:
            return "composite"
    return "prime"

#Prime generator
def prime_gen(k):
    while True:

        #Generate a random number between 2^(k-1) and (2^k) - 1
        p = randint(2**(k-1), 2**k-1)

        #Check if the prime is a prime
        if fermat(p, k) == "prime":
            return p

#Key Generator
def key_gen(k):
    p = prime_gen(k//2)
    q = prime_gen(k//2)
   
    if p != q:
        n = p * q
        phi = (p-1)*(q-1)
        
        e = -1
        while e < 0:
            #Generate random integers between 1 and phi 
            x = randint(1, phi)
            if gcd(x, phi) == 1:
                e = x
                #Compute d using the extended Eucliden algorithm
                d = inverse_mod(e, phi)
        
        return (n, e, d)
    
#Encrypt the message
def encryption(m, n, e):
    return pow(m, e, n)

#Decrypt the message
def decryption(c, n, d):
    return pow(c, d, n)

if __name__ == "__main__":
    #generating the keys
    n, e, d = key_gen(100)
    print('n = '+str(n),'e = '+str(e), 'd = '+str(d))
    
    #message
    m = 12345
    print('message = '+str(m))
    
    #encrypted message
    e = encryption(m, n, e)
    print('Encrypted message = '+str(e))

    #decrypted encrypted message
    d = decryption(e, n, d)
    print('Decrypted message = '+str(d)) 