export type Node = {
  id: string;
};

export type Link = {
  source: string;
  target: string;
};

const links: Link[] = [];
const nodes: Node[] = [];
const n = 100;
const m = 100;
for (let node = 0; node < n * m; node += 1) {
  nodes.push({ id: `${node}` });
  const nextNode = node + 1;
  const bottomNode = node + n;
  const nodeLine = Math.floor(node / n);
  const nextNodeLine = Math.floor(nextNode / n);
  const bottomNodeLine = Math.floor(bottomNode / n);
  if (nodeLine === nextNodeLine)
    links.push({ source: `${node}`, target: `${nextNode}` });
  if (bottomNodeLine < m)
    links.push({ source: `${node}`, target: `${bottomNode}` });
}

export { nodes, links };
