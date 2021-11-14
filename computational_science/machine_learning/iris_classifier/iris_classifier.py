import pandas as pd
from sklearn.neighbors import KNeighborsClassifier

df = pd.read_csv('Iris.csv', sep=',')

df = df.sample(frac=1).reset_index(drop=True)
df = df.drop('Id', axis=1)

df_label = df['Species'].copy()

df = df.drop('Species', axis=1)

amount = 140
train = df.iloc[:amount]
test = df.iloc[amount:]
train_label = df_label.iloc[:amount]
test_label = df_label.iloc[amount:]

clf = KNeighborsClassifier(n_neighbors=21)
clf.fit(train, train_label)

print(clf.score(test, test_label))

test_predict = clf.predict(test)

for i, value in enumerate(test.iterrows()):
        print(f"{value} predicted => {test_predict[i]}")

