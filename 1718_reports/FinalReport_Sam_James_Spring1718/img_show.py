import numpy as np
import matplotlib.pyplot as plt

img_f = open('img_out.txt', 'r')

def is_int(x):
  try:
    int(x)
    return True
  except ValueError:
    return False

while True:
  line = img_f.readline()
  if is_int(line):
    width = int(line)
    break

height = int(img_f.readline())

img = np.empty([height, width, 3], dtype=float)

for h in range(height):
  for w in range(width):
    for i in range(3):
      img[h][w][i] = float(img_f.readline())

plt.imshow(np.clip(img, 0, 1), interpolation='None')
plt.show()

