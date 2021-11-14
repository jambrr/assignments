import pandas as pd
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import confusion_matrix

df = pd.read_csv('./fruit_data.txt', sep='\t')

df = df.sample(frac=1).reset_index(drop=True)

df_label = df['fruit_label'].copy()
df = df[['mass', 'width', 'height', 'color_score']]

amount = 49
train = df.iloc[:amount]
test = df.iloc[amount:]
train_label = df_label.iloc[:amount]
test_label = df_label.iloc[amount:]

clf = KNeighborsClassifier(n_neighbors=3)
clf.fit(train, train_label)

print(clf.score(test, test_label))

test_predict = clf.predict(test)
cm = confusion_matrix(test_label, test_predict)

for i, value in enumerate(test.iterrows()):
    print(f"{value} predicted => {test_predict[i]}")

print(cm)

