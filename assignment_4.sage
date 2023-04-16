from sage.all import *

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
def CopPolyDeg2(a,b,c,Nn):
  n=Nn.nbits()
  X=2^(n//4-2)
  M=matrix(ZZ,[[X^3,a*X^2,b*X,c],\
               [0, Nn*X^3, 0, 0],\
               [0, 0  ,Nn*X^2,0],\
               [0, 0  ,0  ,Nn]])
  V=M.LLL()
  v=V[0]
  R.<x> = ZZ[]
  f=sum(v[i]*x^(3-i)/X^(3-i) for i in range(4))
  return f.roots()

def test():
  """Generates a random polynomial with a small root x0 modulo Nn
     and recovers his root."""
  Nn,p,q,e,d=keyGen()
  n=Nn.nbits()

  x0=ZZ.random_element(3^(n//3-10))
  a=ZZ.random_element(Nn)
  b=mod(-x0^3-a*x0^2,Nn)
  c=mod(-x0^3-a*x0^2-b*x0,Nn)

  print("x0=",x0)
  print(CopPolyDeg2(a,b,c,Nn))

if __name__ == "__main__":
    c = 106931790228306983984681105468768847489271870726744288161524001625441564428928138082764847546330455645721020672129383569332864980247631047991368975076235508102167034128121821366927744907882617928973322081585262275454789559141797135693781509609809801881376951044409238368040773136628352506077437800520604807524
    N = 7093461887837078080679850670284569201984434451509438680075397487008430083469409402872220028910295374993969080568480956718387713777752278390679951948808475707450073714577930152529057057137554724537719912515486712408422541711159469388603174949214558211229232753033853223644282969544939436208933732047182752629

    test()
