with open('app/src/main/java/com/example/ui/AppUI.kt', 'r') as f:
    lines = f.readlines()
d = 0
for i, l in enumerate(lines):
    d += l.count('{') - l.count('}')
    print(f"{i+1:4d} {d:3d}: {l.rstrip()}")
