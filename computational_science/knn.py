import pandas

df = pandas.read_csv('winequality-red.csv', sep=';')

newdf = df.drop('id', 1)
print(df)
