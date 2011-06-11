println("Dumping context!")
def it = context.stringMap("").keySet().iterator()
while(it.hasNext()){
  def name = it.next()
  def value = context.get(name)
  println("\t" + name + " -> " + value)
}