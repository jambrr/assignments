def keyGen(n=256):
  "Generates an RSA key"
  while True:
    p=random_prime(2^(n//2));q=random_prime(2^(n//2));e=3
    if gcd(e,(p-1)*(q-1))==1: break

  d=inverse_mod(e,(p-1)*(q-1))
  Nn=p*q
  print("p=",p,"q=",q)
  print("N=",Nn)
  print("Size of N:",Nn.nbits())
  return Nn,p,q,e,d

"Finds a small root of polynomial x^2+ax+b=0 mod N"
def CopPolyDeg2(a,b,Nn):
  n=Nn.nbits()
  X=2^(n//3-3)
  M=matrix(ZZ,[[X^2,a*X,b],\
               [0  ,Nn*X,0],\
               [0  ,0  ,Nn]])
  V=M.LLL()
  v=V[0]
  R.<x> = ZZ[]
  f=sum(v[i]*x^(2-i)/X^(2-i) for i in range(3))
  return f.roots()

def test():
  """Generates a random polynomial with a small root x0 modulo Nn
     and recovers his root."""
  Nn,p,q,e,d=keyGen()
  n=Nn.nbits()
  x0=ZZ.random_element(2^(n//3-10))
  a=ZZ.random_element(Nn)
  b=mod(-x0^2-a*x0,Nn)
  print("x0=",x0)
  print(a)
  print(b)
  print(CopPolyDeg2(a,b,Nn))

test()
