from sage.all import *
from sage.misc.prandom import randrange

def key_gen():
    p = random_prime(1000)
    q = random_prime(1000)
    
    n = p * q
    sigma = (p-1)*(q-1)
    
    e = -1
    while e < 0
        x = randrange(sigma)
        if gcd(x) == 1:
            e = x
    
    

    

def encryption():
    pass

def decryption():
    pass

if __name__ == "__main__":
    print(key_gen())
    
