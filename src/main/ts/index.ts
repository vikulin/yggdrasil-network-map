import "./styles.css";
import { nodes, links, Node, Link } from "./data-gen";
import { Graph, GraphConfigInterface } from "@cosmograph/cosmos";

const canvas = document.querySelector("canvas") as HTMLCanvasElement;
let graph: Graph<Node, Link>;
const config: GraphConfigInterface<Node, Link> = {
  backgroundColor: "#151515",
  nodeSize: 4,
  nodeColor: "#4B5BBF",
  nodeGreyoutOpacity: 0.1,
  linkWidth: 0.1,
  linkColor: "#5F74C2",
  linkArrows: false,
  linkGreyoutOpacity: 0,
  simulation: {
    linkDistance: 1,
    linkSpring: 2,
    repulsion: 0.2,
    gravity: 0.1,
    decay: 100000
  },
  events: {
    onClick: (node, i, pos, event) => {
      if (node && i !== undefined) {
        graph.selectNodeByIndex(i);
        graph.zoomToNodeByIndex(i);
      } else {
        graph.unselectNodes();
      }
      console.log("Clicked node: ", node);
    }
  }
};

graph = new Graph(canvas, config);
graph.setData(nodes, links);
graph.zoom(0.9);

/* ~ Demo Actions ~ */
// Start / Pause
let isPaused = false;
const pauseButton = document.getElementById("pause") as HTMLDivElement;

function pause() {
  isPaused = true;
  pauseButton.textContent = "Start";
  graph.pause();
}

function start() {
  isPaused = false;
  pauseButton.textContent = "Pause";
  graph.start();
}

function togglePause() {
  if (isPaused) start();
  else pause();
}

pauseButton.addEventListener("click", togglePause);

// Zoom and Select
function getRandomNodeId() {
  return nodes[Math.floor(Math.random() * nodes.length)].id;
}

function getRandomInRange([min, max]: [number, number]): number {
  return Math.random() * (max - min) + min;
}

function fitView() {
  graph.fitView();
}

function zoomIn() {
  const nodeId = getRandomNodeId();
  graph.zoomToNodeById(nodeId);
  graph.selectNodeById(nodeId);
  pause();
}

function selectPoint() {
  const nodeId = getRandomNodeId();
  graph.selectNodeById(nodeId);
  graph.fitView();
  pause();
}

function selectPointsInArea() {
  const w = canvas.clientWidth;
  const h = canvas.clientHeight;
  const left = getRandomInRange([w / 4, w / 2]);
  const right = getRandomInRange([left, (w * 3) / 4]);
  const top = getRandomInRange([h / 4, h / 2]);
  const bottom = getRandomInRange([top, (h * 3) / 4]);
  pause();
  graph.selectNodesInRange([
    [left, top],
    [right, bottom]
  ]);
}

document.getElementById("fit-view")?.addEventListener("click", fitView);
document.getElementById("zoom")?.addEventListener("click", zoomIn);
document.getElementById("select-point")?.addEventListener("click", selectPoint);
document
  .getElementById("select-points-in-area")
  ?.addEventListener("click", selectPointsInArea);
