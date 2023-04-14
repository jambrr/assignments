from sage.all import *
from random import randint

# Function to generate a random prime number
def generate_prime():
    return random_prime(2^512, False, 2^511)

# Function to generate key pairs
def generate_keys():
    p = generate_prime()
    q = generate_prime()
    N = p * q
    phi_N = (p - 1) * (q - 1)
    d = randint(2, phi_N)
    while gcd(d, phi_N) != 1:
        d = randint(2, phi_N)
    e = inverse_mod(d, phi_N)
    return N, e, d, p, q

# Function to sign a message using CRT
def sign_message(msg, N, d, p, q):
    m = Integer(msg.encode("utf-8").hex(), 16)
    sp = power_mod(m, d % (p - 1), p)
    sq = power_mod(m, d % (q - 1), q)
    s = crt([sp, sq], [p, q])
    return s % N

# Function to recover factorization from s and sp_error using Bellcore attack
def recover_factorization(N, sp_error, sq, p, q):
    # Compute u = (s % q) * (q^-1 % p) mod p
    u = (sq * inverse_modulus(q, p)) % p

    # Compute v = (sp_error % p) * (p^-1 % q) mod q
    v = (sp_error * inverse_modulus(p, q)) % q

    # Compute x = (u * q * p + v * p) mod N
    x = (u * q * p + v * p) % N

    # Compute y = (x % 2p) - p
    y = (x % (2 * p)) - p

    # Initialize gcd to None
    gcd = None

    # Keep looping until gcd is not equal to 1 and not equal to N
    while gcd is None or gcd == 1 or gcd == N:
        y += 2 * p
        gcd = gcd(x + y, N)

    factors = [gcd, N // gcd]
    return factors


# Function to detect error in sp using Bellcore attack
def detect_error(N, sp_error, sq, p, q):
    # Compute u = (s % q) * (q^-1 % p) mod p
    u = (sq * inverse_modulus(q, p)) % p

    # Compute v = (sp_error % p) * (p^-1 % q) mod q
    v = (sp_error * inverse_modulus(p, q)) % q

    # Compute x = (u * q * p + v * p) mod N
    x = (u * q * p + v * p) % N

    # Compute y = (x % 2p) - p
    y = (x % (2 * p)) - p

    # If y is zero, it means no error is detected in sp
    if y == 0:
        return False
    else:
        return True



# Generate key pairs
N, e, d, p, q = generate_keys()

# Sign a message using CRT
msg = "Hello, world!"
s = sign_message(msg, N, d, p, q)
print("Signature:", s)

# Simulate error in sp
sp_error = power_mod(randint(1, p - 1), d % (p - 1), p)

# Recover factorization from s and sp_error using Bellcore attack
factorization = recover_factorization(N, sp_error, s % q, p, q)
print("Recovered Factorization:", factorization)

# Detect error in sp using Bellcore attack
error_detected = detect_error(N, sp_error, s % q, p, q)
if error_detected:
    print("Error in sp detected!")
else:
    print("No error detected in sp.")
