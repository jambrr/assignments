from sage.all import *

# Function to generate key pairs
def generate_keys():
    p = random_prime(2^512, False, 2^511)
    q = random_prime(2^512, False, 2^511)
    N = p * q
    phi = (p - 1) * (q - 1)
    d = randint(2, phi)
    while gcd(d, phi) != 1:
        d = randint(2, phi)
    e = inverse_mod(d, phi)
    return N, e, d, p, q

# Function to sign a message using CRT
def sign_message(message, N, d, p, q, error=0):
    m = Integer(message.encode("utf-8").hex(), 16)

    #If error != 0, simulate an error
    sp = power_mod(m, d % (p - 1), p) + error
    
    sq = power_mod(m, d % (q - 1), q)
    s = crt([sp, sq], [p, q]) % N
    
    return (s, sp)

# Function to recover factorization from s and sp_error using Bellcore attack
def recover_factorization(N, sp_error, sq, p, q):
    # Compute u = (s % q) * (q^-1 % p) mod p
    u = (sq * inverse_mod(q, p)) % p

    # Compute v = (sp_error % p) * (p^-1 % q) mod q
    v = (sp_error * inverse_mod(p, q)) % q

    # Compute x = (u * q * p + v * p) mod N
    x = (u * q * p + v * p) % N

    # Compute y = (x % 2p) - p
    y = (x % (2 * p)) - p

    gcd_val = 0

    # Keep looping until gcd is not equal to 1 and not equal to N
    while gcd_val == 0 or gcd_val == 1 or gcd_val == N:
        y += 2 * p
        gcd_val = gcd(x + y, N)

    factors = [gcd_val, N // gcd_val]
    return factors

# Function to detect error in sp
def detect_error(s, s_error):
    if s != s_error:
        return True
    else:
        return False


# Generate key pairs
N, e, d, p, q = generate_keys()

# Sign a message using CRT
message = "Hello world"
s1, sp1 = sign_message(message, N, d, p, q)
print("Signature:", s1)

# Simulate error in sp
print("Simulate error in sp")
error = randint(1, p - 1)
s2, sp2 = sign_message(msg, N, d, p, q, error)
print("Signature:", s2)

# Recover factorization from s and sp using Bellcore attack
factorization = recover_factorization(N, sp2, s2, p, q)
print("Recovered Factorization:", factorization)

# Detect error in sp, by comparing the signtures s1 with s2
error_detected = detect_error(s1, s2)
print(error_detected)