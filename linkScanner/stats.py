import matplotlib.pyplot as plt

accuracy = 97.64
error = 100 - accuracy

# Data for the pie chart
labels = ['Accuracy', 'Error']
sizes = [accuracy, error]
colors = ['#5cb85c', '#d9534f']  # Green for accuracy, Red for error
plt.figure(figsize=(8, 8))

# Plotting the pie chart
plt.pie(sizes, labels=labels, colors=colors, autopct='%1.2f%%', startangle=90)
plt.title('Model Performance (Random Forest Classifier)')

plt.show()






