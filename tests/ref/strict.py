# Usage: python3 tests/ref/strict.py OUT_REF OUT_OURS/dumps
# strict compare: percent of pixels differing by more than 6 per channel (5-bit color rounding allowed)
import sys,glob,os
refd,ourd=sys.argv[1],sys.argv[2]; out=[]
for f in sorted(glob.glob(refd+"/*.ppm")):
    t=os.path.basename(f)[:-4]; o=f"{ourd}/{t}.fb.bgrx"
    if not os.path.exists(o): continue
    d=open(f,"rb").read().split(b"\n",3); w,h=map(int,d[1].split()); a=d[3]; b=open(o,"rb").read()
    n=sum(1 for i in range(w*h) if max(abs(a[i*3]-b[i*4+2]),abs(a[i*3+1]-b[i*4+1]),abs(a[i*3+2]-b[i*4]))>6)
    out.append("%s:%.2f"%(t,n*100.0/(w*h)))
print(" ".join(out))
