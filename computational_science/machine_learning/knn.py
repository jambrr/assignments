import pandas
from sklearn.preprocessing import MinMaxScaler
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import confusion_matrix

df = pandas.read_csv('winequality-red.csv', sep=';')

#newdf = df.drop('id')
quality_df = df['quality'].copy()

quality_df[quality_df <= 5] = 0
quality_df[quality_df > 5] = 1

train = df.iloc[:1000]
test = df.iloc[1000:]
train_label = quality_df.iloc[:1000]
test_label = quality_df.iloc[1000:]

scaler = MinMaxScaler()
scaler.fit(train)
train = scaler.transform(train)
test = scaler.transform(test)

clf = KNeighborsClassifier(n_neighbors=21)
clf.fit(train, train_label)

print(clf.score(test, test_label))

test_predict = clf.predict(test)
cm = confusion_matrix(test, test_predict)
