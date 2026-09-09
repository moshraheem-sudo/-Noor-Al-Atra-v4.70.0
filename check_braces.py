import sys

with open('app/src/main/java/com/example/ui/AppUI.kt', 'r') as f:
    lines = f.readlines()

def check_range(start, end):
    depth = 0
    for i in range(len(lines)):
        line = lines[i]
        depth += line.count('{') - line.count('}')
        if start <= i <= end:
            print(f"{i+1:4d} {depth:3d}: {line.rstrip()}")

check_range(1370, 1410)
