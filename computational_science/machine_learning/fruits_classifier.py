import pandas as pd
from sklearn.neighbors import KNeighborsClassifier

df = pd.read_csv('./fruit_data.txt', sep='\t')

df = df.sample(frac=1).reset_index(drop=True)

df_label = df['fruit_label'].copy()
df = df[['mass', 'width', 'height', 'color_score']]

amount = 10
train = df[amount:]
test = df[:amount]
train_label = df_label[amount:]
test_label = df_label[:amount]

clf = KNeighborsClassifier(n_neighbors=4)
clf.fit(train, train_label)

print(clf.score(test, test_label))

